package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.customer.enums.Grade;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;
import com.project.global.util.ContactHashUtil;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @InjectMocks private CustomerService customerService;

    @Mock private CustomerRepository customerRepository;
    @Mock private AesUtil aesUtil;
    @Mock private ContactHashUtil contactHashUtil;

    @Test
    @DisplayName("[조회] 성공 - 전화번호 기반 유저 조회")
    void loadByPhoneSuccess() {
        // given
        String phoneRaw = "010-1234-5678";
        String contactHash = "hash-base64";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash(contactHash) // ✅ 필수
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 1L);

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(contactHash);
        given(customerRepository.findByContactHash(contactHash)).willReturn(Optional.of(customer));

        // when
        Customer result = customerService.loadByPhone(phoneRaw);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getGrade()).isEqualTo(Grade.GENERAL);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(contactHash);
        verifyNoInteractions(aesUtil);
    }

    @Test
    @DisplayName("[조회] 실패 - 고객 없음")
    void loadByPhoneFailNotFound() {
        // given
        String phoneRaw = "010-1234-5678";
        String contactHash = "hash-base64";

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(contactHash);
        given(customerRepository.findByContactHash(contactHash)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.loadByPhone(phoneRaw))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(contactHash);
        verifyNoInteractions(aesUtil);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 변경(암호화 저장 + 마스킹 응답)")
    void changeEmailEncSuccess() {
        // given
        Long customerId = 1L;
        String plainEmail = "example@example.com";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-base64") // ✅ 필수
                        .emailEnc("old-email-enc")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(plainEmail)).willReturn("new-email-enc");

        // when
        ChangeEmailResponse response =
                customerService.changeEmailEnc(customerId, new ChangeEmailRequest(plainEmail));

        // then
        assertThat(response).isNotNull();
        assertThat(response.maskedEmail()).isEqualTo("e***@example.com");

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(plainEmail);
        verifyNoInteractions(contactHashUtil);

        assertThat(ReflectionTestUtils.getField(customer, "emailEnc")).isEqualTo("new-email-enc");
    }

    @Test
    @DisplayName("[변경] 실패 - 고객 없음")
    void changeEmailEncFailNotFound() {
        // given
        Long customerId = 999L;
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                        () ->
                                customerService.changeEmailEnc(
                                        customerId, new ChangeEmailRequest("a@b.com")))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(aesUtil);
        verifyNoInteractions(contactHashUtil);
    }

    @Test
    @DisplayName("[변경] 성공 - 고객 등급 변경")
    void changeUserGradeSuccess() {
        // given
        Long customerId = 1L;

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.VIP)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-base64") // ✅ 필수
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

        // when
        ChangeGradeResponse response =
                customerService.changeUserGrade(customerId, new ChangeGradeRequest(Grade.GENERAL));

        // then
        assertThat(response).isNotNull();
        assertThat(response.grade()).isEqualTo(Grade.GENERAL);
        assertThat(customer.getGrade()).isEqualTo(Grade.GENERAL);

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(aesUtil);
        verifyNoInteractions(contactHashUtil);
    }

    @Test
    @DisplayName("[변경] 실패 - 고객 등급 변경 실패(고객 없음)")
    void changeUserGradeFailNotFound() {
        // given
        Long customerId = 999L;
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                        () ->
                                customerService.changeUserGrade(
                                        customerId, new ChangeGradeRequest(Grade.GENERAL)))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(aesUtil);
        verifyNoInteractions(contactHashUtil);
    }
}

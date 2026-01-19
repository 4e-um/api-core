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
    @DisplayName("[조회] 성공 - 전화번호(raw) 기반 유저 조회 (hash 변환 후 조회)")
    void loadByPhoneSuccess() {
        // given
        String phoneRaw = "010-1234-5678";
        String hash = "hash-value";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash(hash)
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", 1L);

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(hash);
        given(customerRepository.findByContactHash(hash)).willReturn(Optional.of(customer));

        // when
        Customer result = customerService.loadByPhone(phoneRaw);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getGrade()).isEqualTo(Grade.GENERAL);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(hash);
    }

    @Test
    @DisplayName("[조회] 실패 - 고객 없음 (hash 변환 후 조회)")
    void loadByPhoneFailNotFound() {
        // given
        String phoneRaw = "010-9999-9999";
        String hash = "hash-not-found";

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(hash);
        given(customerRepository.findByContactHash(hash)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.loadByPhone(phoneRaw))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(hash);
    }

    @Test
    @DisplayName("[조회] 실패 - phoneRaw null이면 CUSTOMER_NOT_FOUND")
    void loadByPhoneFailNullPhoneRaw() {
        // given
        String phoneRaw = null;

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(null);
        given(customerRepository.findByContactHash(null)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.loadByPhone(phoneRaw))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(contactHashUtil).hmacSha256Base64(phoneRaw);
        verify(customerRepository).findByContactHash(null);
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
                        .contactHash("hash-value")
                        .emailEnc("old-email-enc")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(plainEmail)).willReturn("new-email-enc");

        ChangeEmailRequest request = new ChangeEmailRequest(plainEmail);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.maskedEmail()).isEqualTo("e***@example.com");

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(plainEmail);

        assertThat(ReflectionTestUtils.getField(customer, "emailEnc")).isEqualTo("new-email-enc");
    }

    @Test
    @DisplayName("[변경] 실패 - 고객 없음 (이메일 변경)")
    void changeEmailEncFailNotFound() {
        // given
        Long customerId = 999L;
        ChangeEmailRequest request = new ChangeEmailRequest("example@example.com");

        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.changeEmailEnc(customerId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(aesUtil);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 마스킹: local 1글자")
    void changeEmailEncMaskLocalLength1() {
        // given
        Long customerId = 1L;
        String email = "a@domain.com";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("old")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(email)).willReturn("enc");

        ChangeEmailRequest request = new ChangeEmailRequest(email);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response.maskedEmail()).isEqualTo("a***@domain.com");

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(email);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 마스킹: local 비어있음")
    void changeEmailEncMaskLocalEmpty() {
        // given
        Long customerId = 1L;
        String email = "@domain.com";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("old")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(email)).willReturn("enc");

        ChangeEmailRequest request = new ChangeEmailRequest(email);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response.maskedEmail()).isEqualTo("***@domain.com");

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(email);
    }

    @Test
    @DisplayName("[변경] 성공 - 이메일 마스킹: @ 없음이면 null")
    void changeEmailEncMaskNoAtReturnsNull() {
        // given
        Long customerId = 1L;
        String email = "not-an-email";

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash-value")
                        .emailEnc("old")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(aesUtil.encrypt(email)).willReturn("enc");

        ChangeEmailRequest request = new ChangeEmailRequest(email);

        // when
        ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

        // then
        assertThat(response.maskedEmail()).isNull();

        verify(customerRepository).findById(customerId);
        verify(aesUtil).encrypt(email);
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
                        .contactHash("hash-value")
                        .emailEnc("encrypted-email")
                        .build();
        ReflectionTestUtils.setField(customer, "customerId", customerId);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

        ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

        // when
        ChangeGradeResponse response = customerService.changeUserGrade(customerId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.grade()).isEqualTo(Grade.GENERAL);
        assertThat(customer.getGrade()).isEqualTo(Grade.GENERAL);

        verify(customerRepository).findById(customerId);
    }

    @Test
    @DisplayName("[변경] 실패 - 고객 등급 변경 실패(고객 없음)")
    void changeUserGradeFailNotFound() {
        // given
        Long customerId = 999L;
        ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.changeUserGrade(customerId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(aesUtil);
    }
}

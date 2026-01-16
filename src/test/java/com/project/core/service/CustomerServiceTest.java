package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @InjectMocks private CustomerService customerService;

  @Mock private CustomerRepository customerRepository;
  @Mock private AesUtil aesUtil;

  @Test
  @DisplayName("[조회] 성공 - 전화번호 기반 유저 조회")
  void loadByContactEncSuccess() {
    // given
    String contactEnc = "encrypted-phone";
    Customer customer =
        Customer.builder()
            .name("홍길동")
            .grade(Grade.GENERAL)
            .contactEnc(contactEnc)
            .emailEnc("encrypted-email")
            .build();
    ReflectionTestUtils.setField(customer, "customerId", 1L);

    given(customerRepository.findByContactEnc(eq(contactEnc))).willReturn(Optional.of(customer));

    // when
    Customer result = customerService.loadByContactEnc(contactEnc);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("홍길동");
    assertThat(result.getGrade()).isEqualTo(Grade.GENERAL);
    verify(customerRepository).findByContactEnc(eq(contactEnc));
  }

  @Test
  @DisplayName("[조회] 실패 - 고객 없음")
  void loadByContactEncFailNotFound() {
    // given
    given(customerRepository.findByContactEnc(eq("encrypted-phone"))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> customerService.loadByContactEnc("encrypted-phone"))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

    verify(customerRepository).findByContactEnc(eq("encrypted-phone"));
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
            .emailEnc("old-email-enc")
            .build();
    ReflectionTestUtils.setField(customer, "customerId", customerId);

    given(customerRepository.findById(eq(customerId))).willReturn(Optional.of(customer));
    given(aesUtil.encrypt(eq(plainEmail))).willReturn("new-email-enc");

    ChangeEmailRequest request = new ChangeEmailRequest(plainEmail);
    // when
    ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.maskedEmail()).isEqualTo("e***@example.com");
    verify(customerRepository).findById(eq(customerId));
    verify(aesUtil).encrypt(eq(plainEmail));

    // 엔티티 내부 emailEnc 변경 확인(필드명 "emailEnc" 기준)
    assertThat(ReflectionTestUtils.getField(customer, "emailEnc")).isEqualTo("new-email-enc");
  }

  @Test
  @DisplayName("[변경] 실패 - 고객 없음")
  void changeEmailEncFailNotFound() {
    // given
    Long customerId = 999L;
    ChangeEmailRequest request = new ChangeEmailRequest("example@example.com");
    given(customerRepository.findById(eq(customerId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> customerService.changeEmailEnc(customerId, request))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

    verify(customerRepository).findById(eq(customerId));
    verifyNoInteractions(aesUtil); // 고객 없으면 encrypt 안 함
  }

  // 마스킹 분기 커버(서비스 레벨에서 response로 검증)
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
            .emailEnc("old")
            .build();
    ReflectionTestUtils.setField(customer, "customerId", customerId);

    given(customerRepository.findById(eq(customerId))).willReturn(Optional.of(customer));
    given(aesUtil.encrypt(eq(email))).willReturn("enc");

    ChangeEmailRequest request = new ChangeEmailRequest(email);
    // when
    ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

    // then
    assertThat(response.maskedEmail()).isEqualTo("a***@domain.com");
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
            .emailEnc("old")
            .build();
    ReflectionTestUtils.setField(customer, "customerId", customerId);

    given(customerRepository.findById(eq(customerId))).willReturn(Optional.of(customer));
    given(aesUtil.encrypt(eq(email))).willReturn("enc");

    ChangeEmailRequest request = new ChangeEmailRequest(email);
    // when
    ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

    // then
    assertThat(response.maskedEmail()).isEqualTo("***@domain.com");
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
            .emailEnc("old")
            .build();
    ReflectionTestUtils.setField(customer, "customerId", customerId);

    given(customerRepository.findById(eq(customerId))).willReturn(Optional.of(customer));
    given(aesUtil.encrypt(eq(email))).willReturn("enc");

    ChangeEmailRequest request = new ChangeEmailRequest(email);
    // when
    ChangeEmailResponse response = customerService.changeEmailEnc(customerId, request);

    // then
    assertThat(response.maskedEmail()).isNull();
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
            .emailEnc("encrypted-email")
            .build();
    ReflectionTestUtils.setField(customer, "customerId", customerId);

    given(customerRepository.findById(eq(customerId))).willReturn(Optional.of(customer));

    ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);
    // when
    ChangeGradeResponse response = customerService.changeUserGrade(customerId, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.grade()).isEqualTo(Grade.GENERAL);
    assertThat(customer.getGrade()).isEqualTo(Grade.GENERAL);
    verify(customerRepository).findById(eq(customerId));
  }

  @Test
  @DisplayName("[변경] 실패 - 고객 등급 변경 실패(고객 없음)")
  void changeUserGradeFailNotFound() {
    // given
    Long customerId = 999L;
    ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);
    given(customerRepository.findById(eq(customerId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> customerService.changeUserGrade(customerId, request))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);

    verify(customerRepository).findById(eq(customerId));
    verifyNoInteractions(aesUtil);
  }
}

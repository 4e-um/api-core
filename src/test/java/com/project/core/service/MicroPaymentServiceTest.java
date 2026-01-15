package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.project.core.controller.dto.response.MicroPaymentResponse;
import com.project.core.infra.entity.micro.MicroPayment;
import com.project.core.infra.entity.micro.enums.MicroPaymentStatus;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.repository.micro.MicroPaymentRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MicroPaymentServiceTest {

  @InjectMocks private MicroPaymentService microPaymentService;

  @Mock private MicroPaymentRepository microPaymentRepository;
  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private Clock clock;

  @BeforeEach
  void setup() {
    given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
    given(clock.instant()).willReturn(Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  @DisplayName("[승인] 성공 - 소액결제 승인")
  void paySuccess() {
    // given
    Long subId = 1L;
    Subscription sub = mock(Subscription.class);
    given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
    given(sub.getSubId()).willReturn(subId);
    given(subscriptionRepository.findById(subId)).willReturn(Optional.of(sub));

    MicroPayment savedPayment =
        MicroPayment.builder().subscription(sub).name("Item").amount(1000).clock(clock).build();
    ReflectionTestUtils.setField(savedPayment, "microId", 100L);

    given(microPaymentRepository.save(any(MicroPayment.class))).willReturn(savedPayment);

    // when
    MicroPaymentResponse response = microPaymentService.pay(subId, "Item", 1000);

    // then
    assertThat(response).isNotNull();
    assertThat(response.microId()).isEqualTo(100L);
    assertThat(response.status()).isEqualTo("BILLED");
    verify(microPaymentRepository).save(any(MicroPayment.class));
  }

  @Test
  @DisplayName("[승인] 실패 - 회선 없음")
  void payFailSubNotFound() {
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.empty());

    assertThatThrownBy(() -> microPaymentService.pay(1L, "Item", 1000))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
  }

  @Test
  @DisplayName("[승인] 실패 - 해지된 회선")
  void payFailSubTerminated() {
    Subscription sub = mock(Subscription.class);
    given(sub.getStatus()).willReturn(SubscriptionStatus.TERMINATED);
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(sub));

    assertThatThrownBy(() -> microPaymentService.pay(1L, "Item", 1000))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
  }

  @Test
  @DisplayName("[승인] 실패 - 유효하지 않은 금액")
  void payFailInvalidAmount() {
    Subscription sub = mock(Subscription.class);
    given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(sub));

    assertThatThrownBy(() -> microPaymentService.pay(1L, "Item", 0))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.INVALID_INPUT_VALUE);

    assertThatThrownBy(() -> microPaymentService.pay(1L, "Item", -100))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.INVALID_INPUT_VALUE);
  }

  @Test
  @DisplayName("[취소] 성공 - 소액결제 취소")
  void cancelSuccess() {
    Long microId = 100L;
    Long subId = 1L;

    Subscription sub = mock(Subscription.class);
    given(sub.getSubId()).willReturn(subId);

    MicroPayment microPayment =
        MicroPayment.builder().subscription(sub).name("Item").amount(1000).clock(clock).build();
    ReflectionTestUtils.setField(microPayment, "microId", microId);

    given(microPaymentRepository.findById(microId)).willReturn(Optional.of(microPayment));

    // when
    MicroPaymentResponse response = microPaymentService.cancel(microId, subId);

    // then
    assertThat(response.status()).isEqualTo("CANCELED");
    assertThat(microPayment.getStatus()).isEqualTo(MicroPaymentStatus.CANCELED);
  }

  @Test
  @DisplayName("[취소] 실패 - 결제 내역 없음")
  void cancelFailNotFound() {
    given(microPaymentRepository.findById(any(Long.class))).willReturn(Optional.empty());

    assertThatThrownBy(() -> microPaymentService.cancel(100L, 1L))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("[취소] 실패 - 내 결제 아님 (Bad Request)")
  void cancelFailForbidden() {
    Long microId = 100L;
    Long subId = 1L;
    Long otherSubId = 2L;

    Subscription sub = mock(Subscription.class);
    given(sub.getSubId()).willReturn(otherSubId); // 주인이 다름

    MicroPayment microPayment =
        MicroPayment.builder().subscription(sub).name("Item").amount(1000).clock(clock).build();

    given(microPaymentRepository.findById(microId)).willReturn(Optional.of(microPayment));

    assertThatThrownBy(() -> microPaymentService.cancel(microId, subId))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST);
  }

  @Test
  @DisplayName("[취소] 실패 - 이미 취소된 결제")
  void cancelFailAlreadyCanceled() {
    Long microId = 100L;
    Long subId = 1L;

    Subscription sub = mock(Subscription.class);
    given(sub.getSubId()).willReturn(subId);

    MicroPayment microPayment =
        MicroPayment.builder().subscription(sub).name("Item").amount(1000).clock(clock).build();
    microPayment.cancel(); // 이미 취소 상태로 변경

    given(microPaymentRepository.findById(microId)).willReturn(Optional.of(microPayment));

    assertThatThrownBy(() -> microPaymentService.cancel(microId, subId))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED);
  }
}

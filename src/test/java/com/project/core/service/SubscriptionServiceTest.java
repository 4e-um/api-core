package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @InjectMocks private SubscriptionService subscriptionService;

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private AesUtil aesUtil; // 현재 미사용이지만 생성자 주입 때문에 필요

  @Test
  @DisplayName("[조회] 성공 - 고객의 회선 목록 조회")
  void findSubscriptionSuccess() {
    // given
    Long customerId = 1L;

    Subscription s1 = newInstanceSubscription(100L);
    Subscription s2 = newInstanceSubscription(200L);

    given(subscriptionRepository.findByCustomer_CustomerId(eq(customerId)))
        .willReturn(List.of(s1, s2));

    // when
    List<Subscription> result = subscriptionService.findSubscription(customerId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).extracting("subId").containsExactlyInAnyOrder(100L, 200L);

    verify(subscriptionRepository).findByCustomer_CustomerId(eq(customerId));
  }

  @Test
  @DisplayName("[조회] 실패 - 고객의 회선 목록이 비어있음")
  void findSubscriptionFailNotFound() {
    // given
    Long customerId = 999L;

    given(subscriptionRepository.findByCustomer_CustomerId(eq(customerId))).willReturn(List.of());

    // when & then
    assertThatThrownBy(() -> subscriptionService.findSubscription(customerId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);

    verify(subscriptionRepository).findByCustomer_CustomerId(eq(customerId));
  }

  // =========================
  // test fixtures
  // =========================

  private static Subscription newInstanceSubscription(Long subId) {
    // Subscription @Builder(Customer customer, String phoneNumber, Clock clock) 시그니처 기준
    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    Customer customer = org.mockito.Mockito.mock(Customer.class);

    Subscription subscription =
        Subscription.builder()
            .customer(customer)
            .phoneNumber("010-1234-5678")
            .clock(fixedClock)
            .build();

    ReflectionTestUtils.setField(subscription, "subId", subId);
    return subscription;
  }
}

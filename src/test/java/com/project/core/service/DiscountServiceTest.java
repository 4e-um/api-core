package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.discount.DiscountPolicy;
import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.discount.enums.Active;
import com.project.core.infra.entity.discount.enums.Category;
import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.TargetScope;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.discount.DiscountPolicyRepository;
import com.project.core.infra.repository.discount.SubscriptionDiscountRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

  @InjectMocks private DiscountService discountService;

  @Mock private SubscriptionDiscountRepository subscriptionDiscountRepository;
  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private DiscountPolicyRepository discountPolicyRepository;

  // =========================
  // loadRequiredBySubId
  // =========================

  @Test
  @DisplayName("[조회] 성공 - 회선 할인 목록 조회")
  void loadRequiredBySubIdSuccess() {
    // given
    Long subId = 1L;

    Subscription subscription = newInstanceSubscription(subId);

    DiscountPolicy policy = newInstanceDiscountPolicy(10L);

    SubscriptionDiscount sd = SubscriptionDiscount.builder()
        .subscription(subscription)
        .discountPolicy(policy)
        .discountType(policy.getDiscountType())
        .value(policy.getValue())
        .targetScope(policy.getTargetScope())
        .startDate(java.time.LocalDateTime.of(2026, 1, 1, 0, 0))
        .build();
    ReflectionTestUtils.setField(sd, "sdId", 100L);

    given(subscriptionDiscountRepository.findBySubscription_SubId(eq(subId)))
        .willReturn(List.of(sd));

    // when
    List<SubscriptionDiscount> result = discountService.loadRequiredBySubId(subId);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSdId()).isEqualTo(100L);
    verify(subscriptionDiscountRepository).findBySubscription_SubId(eq(subId));
  }

  @Test
  @DisplayName("[조회] 실패 - 회선 할인 목록 없음")
  void loadRequiredBySubIdFailNotFound() {
    // given
    Long subId = 1L;
    given(subscriptionDiscountRepository.findBySubscription_SubId(eq(subId))).willReturn(List.of());

    // when & then
    assertThatThrownBy(() -> discountService.loadRequiredBySubId(subId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.DISCOUNT_NOT_FOUND);

    verify(subscriptionDiscountRepository).findBySubscription_SubId(eq(subId));
  }

  // =========================
  // addDiscount
  // =========================

  @Test
  @DisplayName("[추가] 성공 - 할인 적용 생성")
  void addDiscountSuccess() {
    // given
    Long subId = 1L;
    Long discountId = 10L;

    Subscription subscription = newInstanceSubscription(subId);
    DiscountPolicy policy = newInstanceDiscountPolicy(discountId);

    given(subscriptionRepository.findById(eq(subId))).willReturn(Optional.of(subscription));
    given(discountPolicyRepository.findById(eq(discountId))).willReturn(Optional.of(policy));

    given(subscriptionDiscountRepository.save(any(SubscriptionDiscount.class)))
        .willAnswer(invocation -> {
          SubscriptionDiscount arg = invocation.getArgument(0);
          ReflectionTestUtils.setField(arg, "sdId", 100L);
          return arg;
        });

    // when
    Long sdId = discountService.addDiscount(subId, discountId);

    // then
    assertThat(sdId).isEqualTo(100L);
    verify(subscriptionRepository).findById(eq(subId));
    verify(discountPolicyRepository).findById(eq(discountId));
    verify(subscriptionDiscountRepository).save(any(SubscriptionDiscount.class));
  }

  @Test
  @DisplayName("[추가] 실패 - 회선 없음")
  void addDiscountFailSubscriptionNotFound() {
    // given
    Long subId = 999L;
    Long discountId = 10L;

    given(subscriptionRepository.findById(eq(subId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> discountService.addDiscount(subId, discountId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);

    verify(subscriptionRepository).findById(eq(subId));
  }

  @Test
  @DisplayName("[추가] 실패 - 할인 정책 없음")
  void addDiscountFailPolicyNotFound() {
    // given
    Long subId = 1L;
    Long discountId = 999L;

    Subscription subscription = newInstanceSubscription(subId);
    given(subscriptionRepository.findById(eq(subId))).willReturn(Optional.of(subscription));
    given(discountPolicyRepository.findById(eq(discountId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> discountService.addDiscount(subId, discountId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.DISCOUNT_POLICY_NOT_FOUND);

    verify(subscriptionRepository).findById(eq(subId));
    verify(discountPolicyRepository).findById(eq(discountId));
  }

  // =========================
  // changeDiscount
  // =========================

  @Test
  @DisplayName("[변경] 성공 - 기존 할인 종료 후 신규 할인 생성")
  void changeDiscountSuccess() {
    // given
    Long sdId = 100L;
    Long newDiscountId = 20L;
    Long subId = 1L;

    Subscription subscription = newInstanceSubscription(subId);

    // 기존 할인
    DiscountPolicy oldPolicy = newInstanceDiscountPolicy(10L);
    SubscriptionDiscount existing = SubscriptionDiscount.builder()
        .subscription(subscription)
        .discountPolicy(oldPolicy)
        .discountType(oldPolicy.getDiscountType())
        .value(oldPolicy.getValue())
        .targetScope(oldPolicy.getTargetScope())
        .startDate(java.time.LocalDateTime.of(2026, 1, 1, 0, 0))
        .build();
    ReflectionTestUtils.setField(existing, "sdId", sdId);

    given(subscriptionDiscountRepository.findBySdId(eq(sdId))).willReturn(Optional.of(existing));

    // 신규 할인 정책
    DiscountPolicy newPolicy = newInstanceDiscountPolicy(newDiscountId);
    given(subscriptionRepository.findById(eq(subId))).willReturn(Optional.of(subscription));
    given(discountPolicyRepository.findById(eq(newDiscountId))).willReturn(Optional.of(newPolicy));

    // 신규 저장되는 할인 sdId 세팅
    given(subscriptionDiscountRepository.save(any(SubscriptionDiscount.class)))
        .willAnswer(invocation -> {
          SubscriptionDiscount arg = invocation.getArgument(0);
          ReflectionTestUtils.setField(arg, "sdId", 200L);
          return arg;
        });

    // when
    Long newSdId = discountService.changeDiscount(newDiscountId, sdId);

    // then
    assertThat(newSdId).isEqualTo(200L);

    // 기존 할인 종료 되었는지(필드명은 엔티티 구현에 따라 다를 수 있음)
    assertThat(ReflectionTestUtils.getField(existing, "endDate")).isNotNull();

    verify(subscriptionDiscountRepository).findBySdId(eq(sdId));
    verify(subscriptionRepository).findById(eq(subId));
    verify(discountPolicyRepository).findById(eq(newDiscountId));
    verify(subscriptionDiscountRepository).save(any(SubscriptionDiscount.class));
  }

  @Test
  @DisplayName("[변경] 실패 - 기존 할인(sdId) 없음")
  void changeDiscountFailDiscountNotFound() {
    // given
    Long sdId = 999L;
    Long newDiscountId = 20L;

    given(subscriptionDiscountRepository.findBySdId(eq(sdId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> discountService.changeDiscount(newDiscountId, sdId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.DISCOUNT_NOT_FOUND);

    verify(subscriptionDiscountRepository).findBySdId(eq(sdId));
  }

  // changeDiscount 내부에서 addDiscount를 호출하므로, 그 내부 실패도 커버
  @Test
  @DisplayName("[변경] 실패 - 신규 할인 정책 없음")
  void changeDiscountFailNewPolicyNotFound() {
    // given
    Long sdId = 100L;
    Long newDiscountId = 999L;
    Long subId = 1L;

    Subscription subscription = newInstanceSubscription(subId);

    DiscountPolicy oldPolicy = newInstanceDiscountPolicy(10L);
    SubscriptionDiscount existing = SubscriptionDiscount.builder()
        .subscription(subscription)
        .discountPolicy(oldPolicy)
        .discountType(oldPolicy.getDiscountType())
        .value(oldPolicy.getValue())
        .targetScope(oldPolicy.getTargetScope())
        .startDate(java.time.LocalDateTime.of(2026, 1, 1, 0, 0))
        .build();
    ReflectionTestUtils.setField(existing, "sdId", sdId);

    given(subscriptionDiscountRepository.findBySdId(eq(sdId))).willReturn(Optional.of(existing));

    // addDiscount 내부 조회
    given(subscriptionRepository.findById(eq(subId))).willReturn(Optional.of(subscription));
    given(discountPolicyRepository.findById(eq(newDiscountId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> discountService.changeDiscount(newDiscountId, sdId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.DISCOUNT_POLICY_NOT_FOUND);

    verify(subscriptionDiscountRepository).findBySdId(eq(sdId));
    verify(subscriptionRepository).findById(eq(subId));
    verify(discountPolicyRepository).findById(eq(newDiscountId));
  }

  @Test
  @DisplayName("[변경] 실패 - 신규 할인 추가 중 회선 없음")
  void changeDiscountFailSubscriptionNotFoundDuringAdd() {
    // given
    Long sdId = 100L;
    Long newDiscountId = 20L;
    Long subId = 1L;

    Subscription subscription = newInstanceSubscription(subId);

    DiscountPolicy oldPolicy = newInstanceDiscountPolicy(10L);
    SubscriptionDiscount existing = SubscriptionDiscount.builder()
        .subscription(subscription)
        .discountPolicy(oldPolicy)
        .discountType(oldPolicy.getDiscountType())
        .value(oldPolicy.getValue())
        .targetScope(oldPolicy.getTargetScope())
        .startDate(java.time.LocalDateTime.of(2026, 1, 1, 0, 0))
        .build();
    ReflectionTestUtils.setField(existing, "sdId", sdId);

    given(subscriptionDiscountRepository.findBySdId(eq(sdId))).willReturn(Optional.of(existing));

    // addDiscount 내부에서 subscription 조회 실패
    given(subscriptionRepository.findById(eq(subId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> discountService.changeDiscount(newDiscountId, sdId))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);

    verify(subscriptionDiscountRepository).findBySdId(eq(sdId));
    verify(subscriptionRepository).findById(eq(subId));
  }

  // =========================
  // test fixtures
  // =========================

  private static Subscription newInstanceSubscription(Long subId) {
    Clock fixedClock = Clock.fixed(
        java.time.Instant.parse("2026-01-01T00:00:00Z"),
        java.time.ZoneId.of("Asia/Seoul")
    );

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



  private static DiscountPolicy newInstanceDiscountPolicy(Long discountId) {
    DiscountPolicy policy =
        DiscountPolicy.builder()
            .name("테스트 할인")
            .discountType(DiscountType.RATE)
            .value(new BigDecimal("10"))
            .category(Category.PROMOTION)
            .targetScope(TargetScope.PLAN_FEE)
            .active(Active.ACTIVE)
            .build();

    ReflectionTestUtils.setField(policy, "discountId", discountId);
    return policy;
  }

}

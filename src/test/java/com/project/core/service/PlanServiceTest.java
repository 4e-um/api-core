package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.plan.Plan;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.core.infra.repository.plan.PlanRepository;
import com.project.core.infra.repository.plan.SubscriptionPlanRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.core.util.PhoneUtil;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import com.project.global.exception.core.OperationFailedException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import com.project.global.util.AesUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

  @InjectMocks private PlanService planService;

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private PlanRepository planRepository;
  @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private AesUtil aesUtil;
  @Mock private Clock clock;

  // 모든 테스트 실행 전에 Clock 동작 정의
  @BeforeEach
  void setup() {
    // Clock이 null을 반환하지 않도록 설정
    lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
    lenient().when(clock.instant()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  @DisplayName("[가입] 활성회선 0개이고 기존 번호 미사용 중이면 기존 번호 복구")
  void joinSubscription_ReuseNumber() {
    // given
    Long customerId = 1L;
    Customer customer = mock(Customer.class);
    Plan plan = mock(Plan.class);

    given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
    given(planRepository.findById(any(Long.class))).willReturn(Optional.of(plan));

    // 활성 회선 0개
    given(
            subscriptionRepository.countByCustomerAndStatus(
                any(Customer.class), eq(SubscriptionStatus.ACTIVE)))
        .willReturn(0L);

    // 기존 번호 가져오기 & 사용 여부 체크 (미사용)
    given(customer.getContactEnc()).willReturn("oldPhoneEnc");
    given(
            subscriptionRepository.existsByPhoneNumberAndStatus(
                "oldPhoneEnc", SubscriptionStatus.ACTIVE))
        .willReturn(false);

    // when
    planService.joinSubscription(customerId, 1L);

    // then
    // 기존 번호로 저장되었는지 확인
    verify(subscriptionRepository).save(any(Subscription.class));
    verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
  }

  @Test
  @DisplayName("[가입] 활성회선 0개지만 기존 번호가 사용 중이면 새 번호 채번")
  void joinSubscription_NewNumber_WhenOldNumberUsed() {
    try (MockedStatic<PhoneUtil> phoneUtilMock = Mockito.mockStatic(PhoneUtil.class)) {
      // given
      Customer customer = mock(Customer.class);
      given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));
      given(planRepository.findById(any(Long.class))).willReturn(Optional.of(mock(Plan.class)));

      // 활성 회선 0개
      given(
              subscriptionRepository.countByCustomerAndStatus(
                  any(Customer.class), eq(SubscriptionStatus.ACTIVE)))
          .willReturn(0L);

      // 기존 번호 사용중
      given(customer.getContactEnc()).willReturn("oldPhoneEnc");
      given(
              subscriptionRepository.existsByPhoneNumberAndStatus(
                  "oldPhoneEnc", SubscriptionStatus.ACTIVE))
          .willReturn(true);

      // 랜덤 번호 생성
      phoneUtilMock.when(PhoneUtil::generateRandomPhoneNumber).thenReturn("010-1234-5678");
      given(aesUtil.encrypt("010-1234-5678")).willReturn("newPhoneEnc");
      given(subscriptionRepository.existsByPhoneNumber("newPhoneEnc")).willReturn(false);

      // when
      planService.joinSubscription(1L, 1L);

      // then
      verify(subscriptionRepository).save(any(Subscription.class));
    }
  }

  @Test
  @DisplayName("[가입] 활성회선 이미 있으면 새 번호 채번")
  void joinSubscription_NewNumber_WhenHasActiveLines() {
    try (MockedStatic<PhoneUtil> phoneUtilMock = Mockito.mockStatic(PhoneUtil.class)) {
      // given
      Customer customer = mock(Customer.class);
      given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));
      given(planRepository.findById(any(Long.class))).willReturn(Optional.of(mock(Plan.class)));

      // 활성 회선 1개 존재
      given(
              subscriptionRepository.countByCustomerAndStatus(
                  any(Customer.class), eq(SubscriptionStatus.ACTIVE)))
          .willReturn(1L);

      // 랜덤 번호 생성
      phoneUtilMock.when(PhoneUtil::generateRandomPhoneNumber).thenReturn("010-9999-8888");
      given(aesUtil.encrypt("010-9999-8888")).willReturn("randomEnc");
      given(subscriptionRepository.existsByPhoneNumber("randomEnc")).willReturn(false);

      // when
      planService.joinSubscription(1L, 1L);

      // then
      verify(subscriptionRepository).save(any(Subscription.class));
    }
  }

  @Test
  @DisplayName("[가입] 실패 - 고객 정보 없음")
  void joinSubscription_Fail_CustomerNotFound() {
    given(customerRepository.findById(any(Long.class))).willReturn(Optional.empty());

    assertThatThrownBy(() -> planService.joinSubscription(1L, 1L))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);
  }

  @Test
  @DisplayName("[가입] 실패 - 번호 생성 11회 모두 중복")
  void joinSubscription_Fail_NumberGeneration() {
    try (MockedStatic<PhoneUtil> phoneUtilMock = Mockito.mockStatic(PhoneUtil.class)) {
      // given
      given(customerRepository.findById(any(Long.class)))
          .willReturn(Optional.of(mock(Customer.class)));
      given(planRepository.findById(any(Long.class))).willReturn(Optional.of(mock(Plan.class)));
      given(
              subscriptionRepository.countByCustomerAndStatus(
                  any(Customer.class), eq(SubscriptionStatus.ACTIVE)))
          .willReturn(1L);

      phoneUtilMock.when(PhoneUtil::generateRandomPhoneNumber).thenReturn("01000000000");
      given(aesUtil.encrypt(anyString())).willReturn("dupEnc");

      given(subscriptionRepository.existsByPhoneNumber("dupEnc")).willReturn(true);

      // when & then
      assertThatThrownBy(() -> planService.joinSubscription(1L, 1L))
          .isInstanceOf(OperationFailedException.class)
          .extracting("code")
          .isEqualTo(CoreErrorCode.PHONE_NUMBER_GENERATION_FAILED);

      verify(subscriptionRepository, times(11)).existsByPhoneNumber("dupEnc");
    }
  }

  @Test
  @DisplayName("[변경] 성공 - 기존 요금제 만료 처리 후 새 요금제 등록")
  void changePlan_Success() {
    // given
    Subscription sub = mock(Subscription.class);
    given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
    SubscriptionPlan oldHistory = mock(SubscriptionPlan.class);
    Plan newPlan = mock(Plan.class);

    given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));
    given(subscriptionPlanRepository.findActivePlanBySubId(1L)).willReturn(Optional.of(oldHistory));
    given(planRepository.findById(2L)).willReturn(Optional.of(newPlan));

    // when
    planService.changePlan(1L, 2L);

    // then
    verify(oldHistory).expire();
    verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
  }

  @Test
  @DisplayName("[변경] 실패 - 회선 없음")
  void changePlan_Fail_SubNotFound() {
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.empty());

    assertThatThrownBy(() -> planService.changePlan(1L, 2L))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
  }

  @Test
  @DisplayName("[변경] 실패 - 새 요금제 정보 없음")
  void changePlan_Fail_PlanNotFound() {
    // 회선 조회는 성공한다고 가정
    Subscription sub = mock(Subscription.class);
    given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(sub));
    // 회선 상태 체크 통과
    given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);

    // 요금제 조회 실패
    given(planRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // 요금제 이력 조회 (mocking)
    given(subscriptionPlanRepository.findActivePlanBySubId(any(Long.class)))
        .willReturn(Optional.of(mock(SubscriptionPlan.class)));

    assertThatThrownBy(() -> planService.changePlan(1L, 2L))
        .isInstanceOf(EntityNotFoundException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.PLAN_NOT_FOUND);
  }

  @Test
  @DisplayName("[변경] 실패 - 이미 해지된 회선은 변경 불가")
  void changePlan_Fail_AlreadyTerminated() {
    // given
    Subscription sub = new Subscription(null, "phone", clock);
    ReflectionTestUtils.setField(sub, "status", SubscriptionStatus.TERMINATED); // 해지 상태

    given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));

    // when & then
    assertThatThrownBy(() -> planService.changePlan(1L, 2L))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
  }

  @Test
  @DisplayName("[해지] 성공 - 상태 변경 및 요금제 만료")
  void terminateSubscription_Success() {
    // given
    Subscription sub = new Subscription(null, "phone", clock);
    ReflectionTestUtils.setField(sub, "status", SubscriptionStatus.ACTIVE);

    SubscriptionPlan activePlan = mock(SubscriptionPlan.class);

    given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));
    given(subscriptionPlanRepository.findActivePlanBySubId(1L)).willReturn(Optional.of(activePlan));

    // when
    planService.terminateSubscription(1L);

    // then
    assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
    verify(activePlan).expire();
  }

  @Test
  @DisplayName("[해지] 실패 - 이미 해지된 회선")
  void terminateSubscription_Fail_AlreadyTerminated() {
    // given
    Subscription sub = new Subscription(null, "phone", clock);
    ReflectionTestUtils.setField(sub, "status", SubscriptionStatus.TERMINATED);

    given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));

    // when & then
    assertThatThrownBy(() -> planService.terminateSubscription(1L))
        .isInstanceOf(InvalidStateException.class)
        .extracting("code")
        .isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
  }
}

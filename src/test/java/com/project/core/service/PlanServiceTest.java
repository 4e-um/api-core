package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

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

import com.project.core.controller.dto.response.PlanChangeResponse;
import com.project.core.controller.dto.response.SubscriptionJoinResponse;
import com.project.core.controller.dto.response.SubscriptionTerminateResponse;
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
import com.project.global.util.AesUtil;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @InjectMocks private PlanService planService;

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private PlanRepository planRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private AesUtil aesUtil;
    @Mock private Clock clock;

    @BeforeEach
    void setup() {
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("[가입] 활성회선 0개이고 기존 번호 미사용 중이면 기존 번호 복구")
    void joinSubscriptionReuseNumber() {
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

        given(aesUtil.decrypt(anyString())).willReturn("010-1234-5678");

        // when
        SubscriptionJoinResponse response = planService.joinSubscription(customerId, 1L);

        // then
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    @DisplayName("[가입] 활성회선 0개지만 기존 번호가 사용 중이면 새 번호 채번")
    void joinSubscriptionNewNumberWhenOldNumberUsed() {
        try (MockedStatic<PhoneUtil> phoneUtilMock = Mockito.mockStatic(PhoneUtil.class)) {
            // given
            Customer customer = mock(Customer.class);
            given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));
            given(planRepository.findById(any(Long.class)))
                    .willReturn(Optional.of(mock(Plan.class)));

            given(
                            subscriptionRepository.countByCustomerAndStatus(
                                    any(Customer.class), eq(SubscriptionStatus.ACTIVE)))
                    .willReturn(0L);

            given(customer.getContactEnc()).willReturn("oldPhoneEnc");
            given(
                            subscriptionRepository.existsByPhoneNumberAndStatus(
                                    "oldPhoneEnc", SubscriptionStatus.ACTIVE))
                    .willReturn(true);

            phoneUtilMock.when(PhoneUtil::generateRandomPhoneNumber).thenReturn("010-1234-5678");
            given(aesUtil.encrypt("010-1234-5678")).willReturn("newPhoneEnc");
            given(subscriptionRepository.existsByPhoneNumber("newPhoneEnc")).willReturn(false);
            given(aesUtil.decrypt("newPhoneEnc")).willReturn("010-1234-5678");

            // when
            SubscriptionJoinResponse response = planService.joinSubscription(1L, 1L);

            // then
            assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
            verify(subscriptionRepository).save(any(Subscription.class));
        }
    }

    @Test
    @DisplayName("[가입] 활성회선 이미 있으면 새 번호 채번")
    void joinSubscriptionNewNumberWhenHasActiveLines() {
        try (MockedStatic<PhoneUtil> phoneUtilMock = Mockito.mockStatic(PhoneUtil.class)) {
            // given
            Customer customer = mock(Customer.class);
            given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));
            given(planRepository.findById(any(Long.class)))
                    .willReturn(Optional.of(mock(Plan.class)));

            given(
                            subscriptionRepository.countByCustomerAndStatus(
                                    any(Customer.class), eq(SubscriptionStatus.ACTIVE)))
                    .willReturn(1L);

            phoneUtilMock.when(PhoneUtil::generateRandomPhoneNumber).thenReturn("010-9999-8888");
            given(aesUtil.encrypt("010-9999-8888")).willReturn("randomEnc");
            given(subscriptionRepository.existsByPhoneNumber("randomEnc")).willReturn(false);
            given(aesUtil.decrypt("randomEnc")).willReturn("010-9999-8888");

            // when
            SubscriptionJoinResponse response = planService.joinSubscription(1L, 1L);

            // then
            assertThat(response.getPhoneNumber()).isEqualTo("010-9999-8888");
            verify(subscriptionRepository).save(any(Subscription.class));
        }
    }

    @Test
    @DisplayName("[가입] 실패 - 고객 정보 없음")
    void joinSubscriptionFailCustomerNotFound() {
        given(customerRepository.findById(any(Long.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.joinSubscription(1L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Test
    @DisplayName("[가입] 실패 - 요금제 정보 없음")
    void joinSubscriptionFailPlanNotFound() {
        given(customerRepository.findById(any(Long.class)))
                .willReturn(Optional.of(mock(Customer.class)));
        given(planRepository.findById(any(Long.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.joinSubscription(1L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.PLAN_NOT_FOUND);
    }

    @Test
    @DisplayName("[가입] 실패 - 번호 생성 11회 모두 중복")
    void joinSubscriptionFailNumberGeneration() {
        try (MockedStatic<PhoneUtil> phoneUtilMock = Mockito.mockStatic(PhoneUtil.class)) {
            // given
            given(customerRepository.findById(any(Long.class)))
                    .willReturn(Optional.of(mock(Customer.class)));
            given(planRepository.findById(any(Long.class)))
                    .willReturn(Optional.of(mock(Plan.class)));
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
    @DisplayName("[변경] 성공 - 기존 요금제 만료 처리 후 새 요금제 등록 (응답 검증 포함)")
    void changePlanSuccess() {
        // given
        Subscription sub = mock(Subscription.class);
        given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);
        given(sub.getSubId()).willReturn(10L);

        // Old Plan mocking
        Plan oldPlan = mock(Plan.class);
        given(oldPlan.getPlanId()).willReturn(1L);

        SubscriptionPlan oldHistory = mock(SubscriptionPlan.class);
        given(oldHistory.getPlan()).willReturn(oldPlan);

        // New Plan mocking
        Plan newPlan = mock(Plan.class);
        given(newPlan.getPlanId()).willReturn(2L);

        given(subscriptionRepository.findById(10L)).willReturn(Optional.of(sub));
        given(subscriptionPlanRepository.findActivePlanBySubId(10L))
                .willReturn(Optional.of(oldHistory));
        given(planRepository.findById(2L)).willReturn(Optional.of(newPlan));

        // when
        PlanChangeResponse response = planService.changePlan(10L, 2L);

        // then
        assertThat(response.getOldPlanId()).isEqualTo(1L);
        assertThat(response.getNewPlanId()).isEqualTo(2L);
        verify(oldHistory).expire();
        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    @DisplayName("[변경] 실패 - 동일한 요금제로 변경 시도")
    void changePlanFailSamePlan() {
        // given
        Subscription sub = mock(Subscription.class);
        given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);

        // Old Plan mocking (ID=2L)
        Plan oldPlan = mock(Plan.class);
        given(oldPlan.getPlanId()).willReturn(2L);

        SubscriptionPlan oldHistory = mock(SubscriptionPlan.class);
        given(oldHistory.getPlan()).willReturn(oldPlan);

        given(subscriptionRepository.findById(10L)).willReturn(Optional.of(sub));
        given(subscriptionPlanRepository.findActivePlanBySubId(10L))
                .willReturn(Optional.of(oldHistory));

        // when & then
        assertThatThrownBy(() -> planService.changePlan(10L, 2L))
                .isInstanceOf(InvalidStateException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.PLAN_ALREADY_SUBSCRIBED);
    }

    @Test
    @DisplayName("[변경] 실패 - 회선 없음")
    void changePlanFailSubNotFound() {
        given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.changePlan(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("[변경] 실패 - 새 요금제 정보 없음")
    void changePlanFailPlanNotFound() {
        Subscription sub = mock(Subscription.class);
        given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(sub));
        given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);

        // Old Plan mocking
        Plan oldPlan = mock(Plan.class);
        given(oldPlan.getPlanId()).willReturn(1L); // 기존은 1번
        SubscriptionPlan oldHistory = mock(SubscriptionPlan.class);
        given(oldHistory.getPlan()).willReturn(oldPlan);

        given(subscriptionPlanRepository.findActivePlanBySubId(any(Long.class)))
                .willReturn(Optional.of(oldHistory));

        given(planRepository.findById(any(Long.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.changePlan(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.PLAN_NOT_FOUND);
    }

    @Test
    @DisplayName("[변경] 실패 - 현재 사용 중인 요금제 정보 없음")
    void changePlanFailCurrentPlanNotFound() {
        Subscription sub = mock(Subscription.class);
        given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.of(sub));
        given(sub.getStatus()).willReturn(SubscriptionStatus.ACTIVE);

        // Active Plan not found
        given(subscriptionPlanRepository.findActivePlanBySubId(any(Long.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.changePlan(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.PLAN_NOT_FOUND);
    }

    @Test
    @DisplayName("[변경] 실패 - 이미 해지된 회선은 변경 불가")
    void changePlanFailAlreadyTerminated() {
        Subscription sub = new Subscription(null, "phone", clock);
        ReflectionTestUtils.setField(sub, "status", SubscriptionStatus.TERMINATED); // 해지 상태

        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));

        assertThatThrownBy(() -> planService.changePlan(1L, 2L))
                .isInstanceOf(InvalidStateException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("[해지] 성공 - 상태 변경 및 요금제 만료")
    void terminateSubscriptionSuccess() {
        // given
        Subscription sub = new Subscription(null, "phone", clock);
        ReflectionTestUtils.setField(sub, "status", SubscriptionStatus.ACTIVE);
        ReflectionTestUtils.setField(sub, "endDate", LocalDateTime.now());

        SubscriptionPlan activePlan = mock(SubscriptionPlan.class);

        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));
        given(subscriptionPlanRepository.findActivePlanBySubId(1L))
                .willReturn(Optional.of(activePlan));

        // when
        SubscriptionTerminateResponse response = planService.terminateSubscription(1L);

        // then
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.TERMINATED);
        verify(activePlan).expire();
    }

    @Test
    @DisplayName("[해지] 실패 - 회선 정보 없음")
    void terminateSubscriptionFailSubNotFound() {
        given(subscriptionRepository.findById(any(Long.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.terminateSubscription(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("[해지] 실패 - 이미 해지된 회선")
    void terminateSubscriptionFailAlreadyTerminated() {
        Subscription sub = new Subscription(null, "phone", clock);
        ReflectionTestUtils.setField(sub, "status", SubscriptionStatus.TERMINATED);

        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(sub));

        assertThatThrownBy(() -> planService.terminateSubscription(1L))
                .isInstanceOf(InvalidStateException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED);
    }
}

package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.core.controller.dto.response.SubscriptionDetailResponse;
import com.project.core.controller.dto.response.SubscriptionListResponse;
import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.customer.enums.Grade;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.plan.enums.AllotmentPeriod;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.plan.SubscriptionPlanRepository;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.core.infra.repository.usage.UsageSummaryDailyRepository;
import com.project.core.infra.repository.usage.UsageSummaryMonthlyRepository;
import com.project.global.util.AesUtil;
import com.project.global.util.ContactHashUtil;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks private SubscriptionService subscriptionService;

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private UsageSummaryDailyRepository usageSummaryDailyRepository;
    @Mock private UsageSummaryMonthlyRepository usageSummaryMonthlyRepository;
    @Mock private AesUtil aesUtil;
    @Mock private ContactHashUtil contactHashUtil;

    @Test
    @DisplayName("[조회] 성공 - 전체 회선 목록 조회(페이징)")
    void getAllSubscriptionsSuccess() {
        // given
        Pageable pageable = Pageable.unpaged();
        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("enc")
                        .contactHash("hash")
                        .emailEnc("enc-email")
                        .build();

        Subscription sub = newInstanceSubscription(1L, customer);
        Slice<Subscription> slice = new SliceImpl<>(List.of(sub));

        // SubscriptionPlan Mock 설정 (Mockito mock 사용)
        SubscriptionPlan subPlan = org.mockito.Mockito.mock(SubscriptionPlan.class);
        com.project.core.infra.entity.plan.Plan plan =
                org.mockito.Mockito.mock(com.project.core.infra.entity.plan.Plan.class);
        given(plan.getPlanName()).willReturn("BASIC");
        given(subPlan.getPlan()).willReturn(plan);
        given(subPlan.getSubscription()).willReturn(sub);
        given(subPlan.getAllotmentPeriod()).willReturn(AllotmentPeriod.MONTH);
        given(subPlan.getAllotmentAmount()).willReturn(10240L);

        given(subscriptionRepository.findAllSlice(pageable)).willReturn(slice);
        given(subscriptionPlanRepository.findActivePlanBySubId(1L))
                .willReturn(Optional.of(subPlan));
        given(usageSummaryMonthlyRepository.findBySubIdAndPeriod(any(), any()))
                .willReturn(Optional.empty());
        given(aesUtil.decrypt("enc-email")).willReturn("test@example.com");
        given(aesUtil.decrypt("010-1234-5678")).willReturn("010-1234-5678");

        // when
        Slice<SubscriptionListResponse> result = subscriptionService.getAllSubscriptions(pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).customerName()).isEqualTo("홍길동");
        assertThat(result.getContent().get(0).phoneNumber()).isEqualTo("010-**34-**78");

        verify(subscriptionRepository).findAllSlice(pageable);
    }

    @Test
    @DisplayName("[조회] 성공 - 전화번호로 회선 상세 조회 (기본 정보만 응답)")
    void getSubscriptionDetailByPhoneSuccess() {
        // given
        String phoneRaw = "01012345678";
        String hash = "hash-value";
        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("enc")
                        .contactHash("hash")
                        .emailEnc("enc-email")
                        .build();

        Subscription sub = newInstanceSubscription(1L, customer);

        // SubscriptionPlan Mock 설정 (Mockito mock 사용)
        SubscriptionPlan subPlan = org.mockito.Mockito.mock(SubscriptionPlan.class);
        com.project.core.infra.entity.plan.Plan plan =
                org.mockito.Mockito.mock(com.project.core.infra.entity.plan.Plan.class);
        given(plan.getPlanName()).willReturn("BASIC");
        given(subPlan.getPlan()).willReturn(plan);
        given(subPlan.getSubscription()).willReturn(sub);
        given(subPlan.getAllotmentPeriod()).willReturn(AllotmentPeriod.MONTH);
        given(subPlan.getAllotmentAmount()).willReturn(10240L);

        given(contactHashUtil.hmacSha256Base64(phoneRaw)).willReturn(hash);
        given(subscriptionRepository.findByPhoneHash(hash)).willReturn(Optional.of(sub));
        given(subscriptionPlanRepository.findActivePlanBySubId(1L))
                .willReturn(Optional.of(subPlan));
        given(usageSummaryMonthlyRepository.findBySubIdAndPeriod(any(), any()))
                .willReturn(Optional.empty());
        given(aesUtil.decrypt("enc-email")).willReturn("test@example.com");
        given(aesUtil.decrypt("010-1234-5678")).willReturn("010-1234-5678");

        // when
        SubscriptionDetailResponse result =
                subscriptionService.getSubscriptionDetailByPhone(phoneRaw);

        // then
        assertThat(result).isNotNull();
        assertThat(result.subId()).isEqualTo(1L);
        assertThat(result.customerName()).isEqualTo("홍길동");
        assertThat(result.phoneNumber()).isEqualTo("010-**34-**78");

        verify(subscriptionRepository).findByPhoneHash(hash);
    }

    private static Subscription newInstanceSubscription(Long subId, Customer customer) {
        Clock fixedClock =
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        Subscription subscription =
                Subscription.builder()
                        .customer(customer)
                        .phoneNumber("010-1234-5678")
                        .phoneHash("hash")
                        .clock(fixedClock)
                        .build();

        ReflectionTestUtils.setField(subscription, "subId", subId);
        ReflectionTestUtils.setField(subscription, "startDate", LocalDateTime.now(fixedClock));
        return subscription;
    }

    @Test
    @DisplayName("[조회] 성공 - 고객의 회선 목록 조회(응답 DTO 변환)")
    void findSubscriptionResponsesSuccess() {
        // given
        Long customerId = 1L;
        Customer customer = org.mockito.Mockito.mock(Customer.class);
        Subscription s1 = newInstanceSubscription(100L, customer);
        Subscription s2 = newInstanceSubscription(200L, customer);

        given(subscriptionRepository.findAllByCustomer_CustomerIdWithPlan(customerId))
                .willReturn(List.of(s1, s2));

        given(aesUtil.decrypt("010-1234-5678")).willReturn("010-1234-5678");

        // when
        List<SubscriptionResponse> result =
                subscriptionService.findSubscriptionResponses(customerId);

        // then
        assertThat(result).hasSize(2);
        verify(subscriptionRepository).findAllByCustomer_CustomerIdWithPlan(customerId);
    }

    @Test
    @DisplayName("[조회] 성공 - 구독이 없으면 빈 리스트 반환")
    void findSubscription_empty_returnsEmptyList() {
        // given
        Long customerId = 1L;
        given(subscriptionRepository.findAllByCustomer_CustomerIdWithPlan(customerId))
                .willReturn(List.of());

        // when
        List<SubscriptionResponse> result =
                subscriptionService.findSubscriptionResponses(customerId);

        // then
        assertThat(result).isEmpty();
        verify(subscriptionRepository).findAllByCustomer_CustomerIdWithPlan(customerId);
    }
}

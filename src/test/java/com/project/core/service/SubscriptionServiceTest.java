package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks private SubscriptionService subscriptionService;

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private AesUtil aesUtil;

    @Test
    @DisplayName("[조회] 성공 - 고객의 회선 목록 조회(응답 DTO 변환)")
    void findSubscriptionResponsesSuccess() {
        // given
        Long customerId = 1L;

        Subscription s1 = newInstanceSubscription(100L);
        Subscription s2 = newInstanceSubscription(200L);

        given(subscriptionRepository.findByCustomer_CustomerId(customerId))
                .willReturn(List.of(s1, s2));

        // aesUtil이 SubscriptionResponse.from(...) 내부에서 decrypt를 호출할 가능성이 높아서 스텁
        given(aesUtil.decrypt("010-1234-5678")).willReturn("010-1234-5678");

        // when
        List<SubscriptionResponse> result =
                subscriptionService.findSubscriptionResponses(customerId);

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(SubscriptionResponse::subId)
                .containsExactlyInAnyOrder(100L, 200L);

        verify(subscriptionRepository).findByCustomer_CustomerId(customerId);
    }

    // =========================
    // test fixtures
    // =========================

    private static Subscription newInstanceSubscription(Long subId) {
        Clock fixedClock =
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

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

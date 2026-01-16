package com.project.core.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.service.SubscriptionService;
import com.project.global.exception.ExceptionAdvice;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;

@WebMvcTest(SubscriptionController.class)
@Import(ExceptionAdvice.class)
class SubscriptionControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private SubscriptionService subscriptionService;

    @Test
    @DisplayName("[조회/성공] 고객 회선 목록 조회 성공")
    void getSubscriptionsByCustomerSuccess() throws Exception {
        // given
        Long customerId = 1L;

        Subscription s1 = newInstanceSubscription(100L);
        Subscription s2 = newInstanceSubscription(200L);

        given(subscriptionService.findSubscription(eq(customerId))).willReturn(List.of(s1, s2));

        // when & then
        mockMvc.perform(get("/subscriptions/customers/{customerId}", customerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].subId").value(100))
                .andExpect(jsonPath("$[1].subId").value(200));
    }

    @Test
    @DisplayName("[조회/실패] 고객 회선 목록 조회 실패 - 회선 없음(404)")
    void getSubscriptionsByCustomerFailNotFound() throws Exception {
        // given
        Long customerId = 999L;

        given(subscriptionService.findSubscription(eq(customerId)))
                .willThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/subscriptions/customers/{customerId}", customerId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("SUBSCRIPTION_NOT_FOUND"));
    }

    // =========================
    // fixtures
    // =========================

    private static Subscription newInstanceSubscription(Long subId) {
        Clock fixedClock =
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        Customer customer = Mockito.mock(Customer.class);

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

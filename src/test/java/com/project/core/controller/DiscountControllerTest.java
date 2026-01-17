package com.project.core.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.DiscountController.ChangeDiscountRequest;
import com.project.core.controller.DiscountController.CreateDiscountRequest;
import com.project.core.infra.entity.discount.DiscountPolicy;
import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.TargetScope;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.service.DiscountService;
import com.project.global.exception.ExceptionAdvice;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;

@WebMvcTest(DiscountController.class)
@Import(ExceptionAdvice.class)
class DiscountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private DiscountService discountService;

    @Test
    @DisplayName("[조회/성공] 회선 할인 목록 조회 성공")
    void getDiscountsBySubscription_success() throws Exception {
        // given
        Long subId = 1L;

        // Subscription (protected ctor)
        Subscription subscription = newInstance(Subscription.class);
        ReflectionTestUtils.setField(subscription, "subId", subId);
        ReflectionTestUtils.setField(subscription, "phoneNumber", "010-1234-1212"); // 원본

        // DiscountPolicy (protected ctor)
        DiscountPolicy policy = newInstance(DiscountPolicy.class);
        ReflectionTestUtils.setField(policy, "discountId", 10L);

        // SubscriptionDiscount (builder로 생성)
        SubscriptionDiscount sd =
                SubscriptionDiscount.builder()
                        .subscription(subscription)
                        .discountPolicy(policy)
                        .discountType(DiscountType.RATE)
                        .value(new BigDecimal("10"))
                        .targetScope(TargetScope.PLAN_FEE)
                        .startDate(LocalDateTime.of(2026, 1, 1, 0, 0))
                        .build();

        ReflectionTestUtils.setField(sd, "sdId", 100L);

        when(discountService.loadRequiredBySubId(subId)).thenReturn(List.of(sd));

        // when & then
        mockMvc.perform(get("/discounts/subscriptions/{subId}", subId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sdId").value(100))
                .andExpect(jsonPath("$[0].subId").value(1))
                .andExpect(jsonPath("$[0].discountId").value(10))
                .andExpect(jsonPath("$[0].maskedPhoneNumber").value("010-**34-**12")); // 마스킹 결과
    }

    @Test
    @DisplayName("[조회/실패] 할인 회선 목록 조회 실패")
    void getDiscountsBySubscription_notFound() throws Exception {
        // given
        Long subId = 1L;
        when(discountService.loadRequiredBySubId(subId))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.DISCOUNT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/discounts/subscriptions/{subId}", subId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("DISCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.code").value("DISCOUNT_001"));
    }

    @Test
    @DisplayName("[추가/성공] 할인 적용 생성(201)")
    void addDiscount_success() throws Exception {
        // given
        CreateDiscountRequest request = new CreateDiscountRequest();
        request.setSubId(1L);
        request.setDiscountId(10L);

        when(discountService.addDiscount(1L, 10L)).thenReturn(100L);

        // when & then
        mockMvc.perform(
                        post("/discounts/discount/add")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sdId").value(100));
    }

    @Test
    @DisplayName("[변경/성공] 할인 변경(기존 종료 + 신규 생성)")
    void changeDiscount_success() throws Exception {
        // given
        Long sdId = 100L;

        ChangeDiscountRequest request = new ChangeDiscountRequest();
        request.setDiscountId(20L);

        when(discountService.changeDiscount(20L, sdId)).thenReturn(200L);

        // when & then
        mockMvc.perform(
                        patch("/discounts/discount/{sdId}", sdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newSdId").value(200));
    }

    @Test
    @DisplayName("[추가/실패] 할인 적용 생성 실패 - NOT_FOUND(404)")
    void addDiscount_notFound() throws Exception {
        // given
        CreateDiscountRequest request = new CreateDiscountRequest();
        request.setSubId(999L);
        request.setDiscountId(10L);

        when(discountService.addDiscount(999L, 10L))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.DISCOUNT_NOT_FOUND));

        // when & then
        mockMvc.perform(
                        post("/discounts/discount/add")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("DISCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.code").value("DISCOUNT_001"));
    }

    @Test
    @DisplayName("[추가/실패] 할인 적용 생성 실패 - 잘못된 JSON(400)")
    void addDiscount_badRequest_invalidJson() throws Exception {
        // when & then
        mockMvc.perform(
                        post("/discounts/discount/add")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid-json }"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[추가/실패] 할인 적용 생성 실패 - 서버 예외(500)")
    void addDiscount_internalServerError() throws Exception {
        // given
        CreateDiscountRequest request = new CreateDiscountRequest();
        request.setSubId(1L);
        request.setDiscountId(10L);

        when(discountService.addDiscount(1L, 10L)).thenThrow(new RuntimeException("boom"));

        // when & then
        mockMvc.perform(
                        post("/discounts/discount/add")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.code").value("COMMON_004"));
    }

    @Test
    @DisplayName("[변경/실패] 할인 변경 실패 - NOT_FOUND(404)")
    void changeDiscount_notFound() throws Exception {
        // given
        Long sdId = 999L;

        ChangeDiscountRequest request = new ChangeDiscountRequest();
        request.setDiscountId(20L);

        when(discountService.changeDiscount(20L, sdId))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.DISCOUNT_NOT_FOUND));

        // when & then
        mockMvc.perform(
                        patch("/discounts/discount/{sdId}", sdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("DISCOUNT_NOT_FOUND"))
                .andExpect(jsonPath("$.code").value("DISCOUNT_001"));
    }

    @Test
    @DisplayName("[변경/실패] 할인 변경 실패 - 잘못된 JSON(400)")
    void changeDiscount_badRequest_invalidJson() throws Exception {
        // given
        Long sdId = 100L;

        // when & then
        mockMvc.perform(
                        patch("/discounts/discount/{sdId}", sdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid-json }"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[변경/실패] 할인 변경 실패 - 서버 예외(500)")
    void changeDiscount_internalServerError() throws Exception {
        // given
        Long sdId = 100L;

        ChangeDiscountRequest request = new ChangeDiscountRequest();
        request.setDiscountId(20L);

        when(discountService.changeDiscount(20L, sdId)).thenThrow(new RuntimeException("boom"));

        // when & then
        mockMvc.perform(
                        patch("/discounts/discount/{sdId}", sdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.code").value("COMMON_004"));
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> ctor = ReflectionUtils.accessibleConstructor(clazz);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance for test: " + clazz.getName(), e);
        }
    }
}

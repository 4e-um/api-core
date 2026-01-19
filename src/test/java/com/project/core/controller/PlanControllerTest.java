package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.PlanDto;
import com.project.core.infra.entity.plan.enums.AllotmentPeriod;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.service.PlanService;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;

@WebMvcTest(PlanController.class)
class PlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private PlanService planService;

    @Test
    @DisplayName("[조회/성공] 요금제 이력 조회 성공")
    void getPlanHistorySuccess() throws Exception {
        Long subId = 1L;
        PlanDto.HistoryResponse res1 =
                new PlanDto.HistoryResponse(
                        1L,
                        "Plan A",
                        10000,
                        5120L,
                        AllotmentPeriod.MONTH,
                        LocalDateTime.now().minusDays(30),
                        LocalDateTime.now());
        PlanDto.HistoryResponse res2 =
                new PlanDto.HistoryResponse(
                        2L,
                        "Plan B",
                        20000,
                        153600L,
                        AllotmentPeriod.MONTH,
                        LocalDateTime.now(),
                        null);

        when(planService.getPlanHistory(subId)).thenReturn(List.of(res2, res1));

        mockMvc.perform(get("/subscriptions/{subId}/plans", subId).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].planName").value("Plan B"))
                .andExpect(jsonPath("$[1].planName").value("Plan A"));
    }

    @Test
    @DisplayName("[가입/성공] 요금제 가입 요청 성공")
    void joinSubscriptionSuccess() throws Exception {
        PlanDto.JoinRequest request = new PlanDto.JoinRequest(1L, 1L);
        PlanDto.JoinResponse response =
                new PlanDto.JoinResponse(
                        100L,
                        1L,
                        1L,
                        "010-1234-5678",
                        SubscriptionStatus.ACTIVE,
                        LocalDateTime.now());

        when(planService.joinSubscription(any(Long.class), any(Long.class))).thenReturn(response);

        mockMvc.perform(
                        post("/subscriptions")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subId").value(100L))
                .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678"));
    }

    @Test
    @DisplayName("[가입/실패] 존재하지 않는 고객이나 요금제로 가입 시도 시 404 반환")
    void joinSubscriptionFailNotFound() throws Exception {
        PlanDto.JoinRequest request = new PlanDto.JoinRequest(999L, 999L);
        doThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND))
                .when(planService)
                .joinSubscription(any(Long.class), any(Long.class));

        mockMvc.perform(
                        post("/subscriptions")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(CoreErrorCode.CUSTOMER_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.CUSTOMER_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("[변경/성공] 요금제 변경 요청 성공")
    void changePlanSuccess() throws Exception {
        Long subId = 1L;
        PlanDto.ChangeRequest request = new PlanDto.ChangeRequest(2L);
        PlanDto.ChangeResponse response =
                new PlanDto.ChangeResponse(subId, 1L, 2L, LocalDateTime.now());

        when(planService.changePlan(eq(subId), any(Long.class))).thenReturn(response);

        mockMvc.perform(
                        put("/subscriptions/{subId}/plan", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subId").value(1L))
                .andExpect(jsonPath("$.newPlanId").value(2L));
    }

    @Test
    @DisplayName("[변경/실패] 이미 해지된 회선은 변경 불가")
    void changePlanFailAlreadyTerminated() throws Exception {
        Long subId = 1L;
        PlanDto.ChangeRequest request = new PlanDto.ChangeRequest(2L);
        doThrow(new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED))
                .when(planService)
                .changePlan(eq(subId), any(Long.class));

        mockMvc.perform(
                        put("/subscriptions/{subId}/plan", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(
                        status().is(
                                        CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED
                                                .getHttpStatus()
                                                .value()))
                .andExpect(
                        jsonPath("$.title")
                                .value(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED.name()));
    }

    @Test
    @DisplayName("[해지/성공] 요금제 해지 요청 성공")
    void terminateSubscriptionSuccess() throws Exception {
        Long subId = 1L;
        PlanDto.TerminateResponse response =
                new PlanDto.TerminateResponse(
                        subId, SubscriptionStatus.TERMINATED, LocalDateTime.now());

        when(planService.terminateSubscription(subId)).thenReturn(response);

        mockMvc.perform(delete("/subscriptions/{subId}", subId).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subId").value(subId))
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    @Test
    @DisplayName("[해지/실패] 이미 해지된 회선을 다시 해지 시도 시 400 반환")
    void terminateSubscriptionFailAlreadyTerminated() throws Exception {
        Long subId = 1L;
        doThrow(new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED))
                .when(planService)
                .terminateSubscription(any(Long.class));

        mockMvc.perform(delete("/subscriptions/{subId}", subId).with(csrf()))
                .andDo(print())
                .andExpect(
                        status().is(
                                        CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED
                                                .getHttpStatus()
                                                .value()))
                .andExpect(
                        jsonPath("$.title")
                                .value(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED.name()));
    }
}

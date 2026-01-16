package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.request.PlanChangeRequest;
import com.project.core.controller.dto.request.SubscriptionJoinRequest;
import com.project.core.controller.dto.response.PlanChangeResponse;
import com.project.core.controller.dto.response.SubscriptionJoinResponse;
import com.project.core.controller.dto.response.SubscriptionTerminateResponse;
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
    @DisplayName("[가입/성공] 요금제 가입 요청 성공")
    void joinSubscriptionSuccess() throws Exception {
        SubscriptionJoinRequest request = new SubscriptionJoinRequest(1L, 1L);
        SubscriptionJoinResponse response =
                SubscriptionJoinResponse.builder()
                        .subId(100L)
                        .customerId(1L)
                        .planId(1L)
                        .phoneNumber("010-1234-5678")
                        .status(SubscriptionStatus.ACTIVE)
                        .startDate(LocalDateTime.now())
                        .build();

        when(planService.joinSubscription(any(Long.class), any(Long.class))).thenReturn(response);

        mockMvc.perform(
                        post("/plan/join")
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
        SubscriptionJoinRequest request = new SubscriptionJoinRequest(999L, 999L);
        doThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND))
                .when(planService)
                .joinSubscription(any(Long.class), any(Long.class));

        mockMvc.perform(
                        post("/plan/join")
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
        PlanChangeRequest request = new PlanChangeRequest(1L, 2L);
        PlanChangeResponse response =
                PlanChangeResponse.builder()
                        .subId(1L)
                        .newPlanId(2L)
                        .changedAt(LocalDateTime.now())
                        .build();

        when(planService.changePlan(any(Long.class), any(Long.class))).thenReturn(response);

        mockMvc.perform(
                        post("/plan/change")
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
        PlanChangeRequest request = new PlanChangeRequest(1L, 2L);
        doThrow(new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED))
                .when(planService)
                .changePlan(any(Long.class), any(Long.class));

        mockMvc.perform(
                        post("/plan/change")
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
        SubscriptionTerminateResponse response =
                SubscriptionTerminateResponse.builder()
                        .subId(subId)
                        .status(SubscriptionStatus.TERMINATED)
                        .terminatedAt(LocalDateTime.now())
                        .build();

        when(planService.terminateSubscription(subId)).thenReturn(response);

        mockMvc.perform(post("/plan/{subId}/terminate", subId).with(csrf()))
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

        mockMvc.perform(post("/plan/{subId}/terminate", subId).with(csrf()))
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

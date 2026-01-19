package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.project.core.controller.dto.MicroPaymentDto;
import com.project.core.service.MicroPaymentService;
import com.project.global.exception.code.domain.GlobalErrorCode;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;

@WebMvcTest(MicroPaymentController.class)
class MicroPaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private MicroPaymentService microPaymentService;

    @Test
    @DisplayName("[조회/성공] 소액결제 이력 조회 성공")
    void getMicroPaymentHistorySuccess() throws Exception {
        Long subId = 1L;
        MicroPaymentDto.HistoryResponse res1 =
                new MicroPaymentDto.HistoryResponse(
                        1L, "Item A", 1000, LocalDateTime.now().minusDays(1), "BILLED");
        MicroPaymentDto.HistoryResponse res2 =
                new MicroPaymentDto.HistoryResponse(
                        2L, "Item B", 2000, LocalDateTime.now(), "CANCELED");

        when(microPaymentService.getMicroPaymentHistory(subId)).thenReturn(List.of(res2, res1));

        mockMvc.perform(get("/subscriptions/{subId}/micropayments", subId).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Item B"))
                .andExpect(jsonPath("$[1].name").value("Item A"));
    }

    @Test
    @DisplayName("[결제/성공] 소액결제 요청 성공")
    void paySuccess() throws Exception {
        Long subId = 1L;
        MicroPaymentDto.Request request = new MicroPaymentDto.Request("Game Item", 5000);
        MicroPaymentDto.Response response =
                new MicroPaymentDto.Response(
                        100L, 1L, "Game Item", 5000, LocalDateTime.now(), "BILLED");

        when(microPaymentService.pay(eq(subId), any(String.class), any(Integer.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/subscriptions/{subId}/micropayments", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.microId").value(100L))
                .andExpect(jsonPath("$.status").value("BILLED"));
    }

    @Test
    @DisplayName("[결제/실패] 존재하지 않는 회선으로 결제 시도 시 404 반환")
    void payFailSubNotFound() throws Exception {
        Long subId = 999L;
        MicroPaymentDto.Request request = new MicroPaymentDto.Request("Item", 1000);
        when(microPaymentService.pay(eq(subId), any(String.class), any(Integer.class)))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND));

        mockMvc.perform(
                        post("/subscriptions/{subId}/micropayments", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(
                        status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("[결제/실패] 해지된 회선으로 결제 시도 시 400 반환")
    void payFailSubTerminated() throws Exception {
        Long subId = 1L;
        MicroPaymentDto.Request request = new MicroPaymentDto.Request("Item", 1000);
        when(microPaymentService.pay(eq(subId), any(String.class), any(Integer.class)))
                .thenThrow(
                        new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED));

        mockMvc.perform(
                        post("/subscriptions/{subId}/micropayments", subId)
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
    @DisplayName("[결제/실패] 유효하지 않은 금액으로 결제 시도 시 400 반환 (Validation Check)")
    void payFailInvalidAmount() throws Exception {
        Long subId = 1L;
        MicroPaymentDto.Request request = new MicroPaymentDto.Request("Item", -100);
        // Validation에서 걸러지므로 Service는 호출되지 않음

        mockMvc.perform(
                        post("/subscriptions/{subId}/micropayments", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(
                        status().is(
                                        GlobalErrorCode.METHOD_ARGUMENT_NOT_VALID
                                                .getHttpStatus()
                                                .value()))
                .andExpect(
                        jsonPath("$.title")
                                .value(GlobalErrorCode.METHOD_ARGUMENT_NOT_VALID.name()));
    }

    @Test
    @DisplayName("[취소/성공] 소액결제 취소 요청 성공")
    void cancelSuccess() throws Exception {
        Long subId = 1L;
        Long microId = 100L;
        MicroPaymentDto.Response response =
                new MicroPaymentDto.Response(
                        microId, 1L, "Item", 5000, LocalDateTime.now(), "CANCELED");

        when(microPaymentService.cancel(microId, subId)).thenReturn(response);

        mockMvc.perform(
                        post(
                                        "/subscriptions/{subId}/micropayments/{microId}/cancel",
                                        subId,
                                        microId)
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @DisplayName("[취소/실패] 존재하지 않는 결제 내역 취소 시도 시 404 반환")
    void cancelFailNotFound() throws Exception {
        Long subId = 1L;
        Long microId = 999L;
        when(microPaymentService.cancel(microId, subId))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND));

        mockMvc.perform(
                        post(
                                        "/subscriptions/{subId}/micropayments/{microId}/cancel",
                                        subId,
                                        microId)
                                .with(csrf()))
                .andDo(print())
                .andExpect(
                        status().is(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("[취소/실패] 다른 회선의 결제 내역 취소 시도 시 400 반환")
    void cancelFailForbidden() throws Exception {
        Long subId = 2L; // 다른 subId
        Long microId = 100L;
        when(microPaymentService.cancel(microId, subId))
                .thenThrow(new InvalidStateException(CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST));

        mockMvc.perform(
                        post(
                                        "/subscriptions/{subId}/micropayments/{microId}/cancel",
                                        subId,
                                        microId)
                                .with(csrf()))
                .andDo(print())
                .andExpect(
                        status().is(
                                        CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST
                                                .getHttpStatus()
                                                .value()))
                .andExpect(
                        jsonPath("$.title").value(CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST.name()));
    }

    @Test
    @DisplayName("[취소/실패] 이미 취소된 결제 내역 다시 취소 시도 시 400 반환")
    void cancelFailAlreadyCanceled() throws Exception {
        Long subId = 1L;
        Long microId = 100L;
        when(microPaymentService.cancel(microId, subId))
                .thenThrow(new InvalidStateException(CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED));

        mockMvc.perform(
                        post(
                                        "/subscriptions/{subId}/micropayments/{microId}/cancel",
                                        subId,
                                        microId)
                                .with(csrf()))
                .andDo(print())
                .andExpect(
                        status().is(
                                        CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED
                                                .getHttpStatus()
                                                .value()))
                .andExpect(
                        jsonPath("$.title")
                                .value(CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED.name()));
    }
}

package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.request.MicroPaymentCancelRequest;
import com.project.core.controller.dto.request.MicroPaymentRequest;
import com.project.core.controller.dto.response.MicroPaymentResponse;
import com.project.core.service.MicroPaymentService;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MicroPaymentController.class)
class MicroPaymentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private MicroPaymentService microPaymentService;

  @Test
  @DisplayName("[결제/성공] 소액결제 요청 성공")
  void paySuccess() throws Exception {
    MicroPaymentRequest request = new MicroPaymentRequest(1L, "Game Item", 5000);
    MicroPaymentResponse response =
        new MicroPaymentResponse(100L, 1L, "Game Item", 5000, LocalDateTime.now(), "BILLED");

    when(microPaymentService.pay(any(Long.class), any(String.class), any(Integer.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/micro-payment/pay")
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
    MicroPaymentRequest request = new MicroPaymentRequest(999L, "Item", 1000);
    doThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND))
        .when(microPaymentService)
        .pay(any(Long.class), any(String.class), any(Integer.class));

    mockMvc
        .perform(
            post("/micro-payment/pay")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
  }

  @Test
  @DisplayName("[결제/실패] 해지된 회선으로 결제 시도 시 400 반환")
  void payFailSubTerminated() throws Exception {
    MicroPaymentRequest request = new MicroPaymentRequest(1L, "Item", 1000);
    doThrow(new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED))
        .when(microPaymentService)
        .pay(any(Long.class), any(String.class), any(Integer.class));

    mockMvc
        .perform(
            post("/micro-payment/pay")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(
            status().is(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED.name()));
  }

  @Test
  @DisplayName("[결제/실패] 유효하지 않은 금액으로 결제 시도 시 400 반환")
  void payFailInvalidAmount() throws Exception {
    MicroPaymentRequest request = new MicroPaymentRequest(1L, "Item", -100);
    doThrow(new InvalidStateException(CoreErrorCode.INVALID_INPUT_VALUE))
        .when(microPaymentService)
        .pay(any(Long.class), any(String.class), any(Integer.class));

    mockMvc
        .perform(
            post("/micro-payment/pay")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.INVALID_INPUT_VALUE.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.INVALID_INPUT_VALUE.name()));
  }

  @Test
  @DisplayName("[취소/성공] 소액결제 취소 요청 성공")
  void cancelSuccess() throws Exception {
    Long microId = 100L;
    MicroPaymentCancelRequest request = new MicroPaymentCancelRequest(1L);
    MicroPaymentResponse response =
        new MicroPaymentResponse(microId, 1L, "Item", 5000, LocalDateTime.now(), "CANCELED");

    when(microPaymentService.cancel(eq(microId), any(Long.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/micro-payment/{microId}/cancel", microId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"));
  }

  @Test
  @DisplayName("[취소/실패] 존재하지 않는 결제 내역 취소 시도 시 404 반환")
  void cancelFailNotFound() throws Exception {
    Long microId = 999L;
    MicroPaymentCancelRequest request = new MicroPaymentCancelRequest(1L);
    doThrow(new EntityNotFoundException(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND))
        .when(microPaymentService)
        .cancel(eq(microId), any(Long.class));

    mockMvc
        .perform(
            post("/micro-payment/{microId}/cancel", microId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.MICRO_PAYMENT_NOT_FOUND.name()));
  }

  @Test
  @DisplayName("[취소/실패] 다른 회선의 결제 내역 취소 시도 시 400 반환")
  void cancelFailForbidden() throws Exception {
    Long microId = 100L;
    MicroPaymentCancelRequest request = new MicroPaymentCancelRequest(2L); // 다른 subId
    doThrow(new InvalidStateException(CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST))
        .when(microPaymentService)
        .cancel(eq(microId), any(Long.class));

    mockMvc
        .perform(
            post("/micro-payment/{microId}/cancel", microId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST.name()));
  }

  @Test
  @DisplayName("[취소/실패] 이미 취소된 결제 내역 다시 취소 시도 시 400 반환")
  void cancelFailAlreadyCanceled() throws Exception {
    Long microId = 100L;
    MicroPaymentCancelRequest request = new MicroPaymentCancelRequest(1L);
    doThrow(new InvalidStateException(CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED))
        .when(microPaymentService)
        .cancel(eq(microId), any(Long.class));

    mockMvc
        .perform(
            post("/micro-payment/{microId}/cancel", microId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(
            status().is(CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.MICRO_PAYMENT_ALREADY_CANCELED.name()));
  }
}

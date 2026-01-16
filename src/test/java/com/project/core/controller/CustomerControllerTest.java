package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.request.PhoneSearchRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.customer.enums.Grade;
import com.project.core.service.CustomerService;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CustomerService customerService;

  @Test
  @DisplayName("[조회/성공] 전화번호 기반 유저 조회 성공")
  void loadByContactEncSuccess() throws Exception {
    // given
    PhoneSearchRequest request = new PhoneSearchRequest("encrypted-phone");

    Customer customer =
        Customer.builder()
            .name("홍길동")
            .grade(Grade.GENERAL)
            .contactEnc("encrypted-phone")
            .emailEnc("encrypted-email")
            .build();

    ReflectionTestUtils.setField(customer, "customerId", 1L);

    when(customerService.loadByContactEnc(eq("encrypted-phone"))).thenReturn(customer);

    // when & then
    mockMvc
        .perform(
            post("/customer/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerId").value(1L))
        .andExpect(jsonPath("$.name").value("홍길동"))
        .andExpect(jsonPath("$.grade").value("GENERAL"));
  }

  @Test
  @DisplayName("[조회/실패] 전화번호 기반 유저 조회 실패 - 고객 없음")
  void loadByContactEncFail_notFound() throws Exception {
    // given
    PhoneSearchRequest request = new PhoneSearchRequest("encrypted-phone");

    when(customerService.loadByContactEnc(eq("encrypted-phone")))
        .thenThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

    // when & then
    mockMvc
        .perform(
            post("/customer/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[변경/성공] 유저 이메일 변경 성공")
  void changeEmailSuccess() throws Exception {
    // given
    Long customerId = 1L;
    ChangeEmailRequest request = new ChangeEmailRequest("example@example.com");

    // NOTE: 현재 DTO가 emailEnc 하나면 jsonPath는 $.emailEnc 가 맞음
    ChangeEmailResponse response = new ChangeEmailResponse("encrypted-or-masked-email");

    when(customerService.changeEmailEnc(eq(customerId), any(ChangeEmailRequest.class)))
        .thenReturn(response);

    // when & then
    mockMvc
        .perform(
            post("/customer/{customerId}/email", customerId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.maskedEmail").value("encrypted-or-masked-email"));
  }

  @Test
  @DisplayName("[변경/실패] 존재하지 않는 유저 이메일 변경 실패")
  void changeEmailFail_notFound() throws Exception {
    // given
    Long customerId = 999L;
    ChangeEmailRequest request = new ChangeEmailRequest("example@example.com");

    when(customerService.changeEmailEnc(eq(customerId), any(ChangeEmailRequest.class)))
        .thenThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

    // when & then
    mockMvc
        .perform(
            post("/customer/{customerId}/email", customerId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[변경/성공] 고객 등급 변경 성공")
  void changeGradeSuccess() throws Exception {
    // given
    Long customerId = 1L;
    ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

    ChangeGradeResponse response = new ChangeGradeResponse(Grade.GENERAL);

    when(customerService.changeUserGrade(eq(customerId), any(ChangeGradeRequest.class)))
        .thenReturn(response);

    // when & then
    mockMvc
        .perform(
            post("/customer/{customerId}/grade", customerId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.grade").value("GENERAL"));
  }

  @Test
  @DisplayName("[변경/실패] 존재하지 않는 고객 등급 변경 실패")
  void changeGradeFail_notFound() throws Exception {
    // given
    Long customerId = 999L;
    ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

    when(customerService.changeUserGrade(eq(customerId), any(ChangeGradeRequest.class)))
        .thenThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

    // when & then
    mockMvc
        .perform(
            post("/customer/{customerId}/grade", customerId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound());
  }
}

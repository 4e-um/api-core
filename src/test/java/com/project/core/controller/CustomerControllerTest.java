package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.request.PhoneSearchRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.customer.enums.Grade;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.service.CustomerService;
import com.project.core.service.SubscriptionService;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CustomerService customerService;
    @MockitoBean private SubscriptionService subscriptionService; // ✅ 추가

    @Test
    @DisplayName("[조회/성공] 전화번호 기반 유저 조회 성공")
    void searchByPhoneSuccess() throws Exception {
        // given
        PhoneSearchRequest request = new PhoneSearchRequest("01012345678"); // phoneRaw

        Customer customer =
                Customer.builder()
                        .name("홍길동")
                        .grade(Grade.GENERAL)
                        .contactEnc("encrypted-phone")
                        .contactHash("hash") // ✅ 엔티티가 nullable=false이므로 채움
                        .emailEnc("encrypted-email")
                        .build();

        ReflectionTestUtils.setField(customer, "customerId", 1L);

        List<SubscriptionResponse> subs =
                List.of(new SubscriptionResponse(
                        10L,
                        "010-****-5678",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(30),
                        SubscriptionStatus.ACTIVE,
                        15
                ));

        when(customerService.loadByPhone(eq("01012345678"))).thenReturn(customer);
        when(subscriptionService.findSubscriptionResponses(eq(1L))).thenReturn(subs); // ✅ 추가

        // when & then
        mockMvc.perform(
                        post("/customer/search")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.name").value("홍길동"))
                // ✅ 이제 grade가 아니라 subscriptions를 검증
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.subscriptions.length()").value(1))
                .andExpect(jsonPath("$.subscriptions[0].subId").value(10L))
                .andExpect(jsonPath("$.subscriptions[0].maskedPhoneNumber").value("010-****-5678"));
    }

    @Test
    @DisplayName("[조회/실패] 전화번호 기반 유저 조회 실패 - 고객 없음")
    void searchByPhoneFailNotFound() throws Exception {
        // given
        PhoneSearchRequest request = new PhoneSearchRequest("01012345678");

        when(customerService.loadByPhone(eq("01012345678")))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

        // when & then
        mockMvc.perform(
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

        ChangeEmailResponse response = new ChangeEmailResponse("encrypted-or-masked-email");

        when(customerService.changeEmailEnc(eq(customerId), any(ChangeEmailRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
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
    void changeEmailFailNotFound() throws Exception {
        // given
        Long customerId = 999L;
        ChangeEmailRequest request = new ChangeEmailRequest("example@example.com");

        when(customerService.changeEmailEnc(eq(customerId), any(ChangeEmailRequest.class)))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

        // when & then
        mockMvc.perform(
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
        mockMvc.perform(
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
    void changeGradeFailNotFound() throws Exception {
        // given
        Long customerId = 999L;
        ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

        when(customerService.changeUserGrade(eq(customerId), any(ChangeGradeRequest.class)))
                .thenThrow(new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

        // when & then
        mockMvc.perform(
                        post("/customer/{customerId}/grade", customerId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // =========================
    // [공통 실패] /customer/search
    // =========================

    @Test
    @DisplayName("[조회/실패] 전화번호 기반 유저 조회 실패 - 잘못된 JSON(400)")
    void searchByPhoneFailInvalidJsonBadRequest() throws Exception {
        mockMvc.perform(
                        post("/customer/search")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid-json }"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[조회/실패] 전화번호 기반 유저 조회 실패 - 서버 예외(500)")
    void searchByPhoneFailInternalServerError() throws Exception {
        // given
        PhoneSearchRequest request = new PhoneSearchRequest("01012345678");

        when(customerService.loadByPhone(eq("01012345678")))
                .thenThrow(new RuntimeException("boom"));

        // when & then
        mockMvc.perform(
                        post("/customer/search")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.code").value("COMMON_004"));
    }

    // =========================
    // [공통 실패] /customer/{customerId}/email
    // =========================

    @Test
    @DisplayName("[변경/실패] 유저 이메일 변경 실패 - 잘못된 JSON(400)")
    void changeEmailFail_invalidJson_badRequest() throws Exception {
        Long customerId = 1L;

        mockMvc.perform(
                        post("/customer/{customerId}/email", customerId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid-json }"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[변경/실패] 유저 이메일 변경 실패 - 서버 예외(500)")
    void changeEmailFail_internalServerError() throws Exception {
        Long customerId = 1L;
        ChangeEmailRequest request = new ChangeEmailRequest("example@example.com");

        when(customerService.changeEmailEnc(eq(customerId), any(ChangeEmailRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(
                        post("/customer/{customerId}/email", customerId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.code").value("COMMON_004"));
    }

    // =========================
    // [공통 실패] /customer/{customerId}/grade
    // =========================

    @Test
    @DisplayName("[변경/실패] 고객 등급 변경 실패 - 잘못된 JSON(400)")
    void changeGradeFail_invalidJson_badRequest() throws Exception {
        Long customerId = 1L;

        mockMvc.perform(
                        post("/customer/{customerId}/grade", customerId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid-json }"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[변경/실패] 고객 등급 변경 실패 - 서버 예외(500)")
    void changeGradeFail_internalServerError() throws Exception {
        Long customerId = 1L;
        ChangeGradeRequest request = new ChangeGradeRequest(Grade.GENERAL);

        when(customerService.changeUserGrade(eq(customerId), any(ChangeGradeRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(
                        post("/customer/{customerId}/grade", customerId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.code").value("COMMON_004"));
    }
}

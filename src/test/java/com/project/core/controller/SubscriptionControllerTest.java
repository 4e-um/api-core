package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.request.PhoneSearchRequest;
import com.project.core.controller.dto.response.SubscriptionDetailResponse;
import com.project.core.controller.dto.response.SubscriptionListResponse;
import com.project.core.service.SubscriptionService;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private SubscriptionService subscriptionService;

    @Test
    @DisplayName("[조회/성공] 전체 회선 목록 조회 성공")
    void getAllSubscriptionsSuccess() throws Exception {
        // given
        SubscriptionListResponse response =
                SubscriptionListResponse.builder()
                        .subId(1L)
                        .customerName("홍길동")
                        .phoneNumber("010-**34-**78")
                        .status("ACTIVE")
                        .build();

        Page<SubscriptionListResponse> pageResponse = new PageImpl<>(List.of(response));

        when(subscriptionService.getAllSubscriptions(any(Pageable.class))).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(
                        get("/subscriptions")
                                .param("page", "0")
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("홍길동"));
    }

    @Test
    @DisplayName("[조회/성공] 전화번호 기반 회선 상세 조회 성공")
    void searchSubscriptionSuccess() throws Exception {
        // given
        PhoneSearchRequest request = new PhoneSearchRequest("01012345678");
        SubscriptionDetailResponse detailResponse =
                SubscriptionDetailResponse.builder()
                        .subId(1L)
                        .phoneNumber("010-**34-**78")
                        .status("ACTIVE")
                        .build();

        when(subscriptionService.getSubscriptionDetailByPhone("01012345678"))
                .thenReturn(detailResponse);

        // when & then
        mockMvc.perform(
                        post("/subscriptions/search")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subId").value(1L))
                .andExpect(jsonPath("$.phoneNumber").value("010-**34-**78"));
    }
}

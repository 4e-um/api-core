package com.project.core.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.core.controller.dto.GrafanaInfoDto;
import com.project.core.controller.dto.response.BillingDashboardResponse;
import com.project.core.service.InvoiceService;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;

@WebMvcTest(InvoiceController.class)
class BillingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private InvoiceService invoiceService;

    @Test
    @DisplayName("[조회/성공] 배치 대시보드 조회 성공")
    void getBillingDashboard_success() throws Exception {
        // given
        BillingDashboardResponse response =
                BillingDashboardResponse.builder()
                        .batchJobs(List.of())
                        .grafana(
                                GrafanaInfoDto.builder()
                                        .iframeUrl("https://grafana.example.com")
                                        .refreshInterval("5s")
                                        .build())
                        .recentFailures(List.of())
                        .build();

        given(invoiceService.getDashboard()).willReturn(response);

        // when & then
        mockMvc.perform(get("/billing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grafana.iframeUrl").value("https://grafana.example.com"))
                .andExpect(jsonPath("$.batchJobs").isArray())
                .andExpect(jsonPath("$.recentFailures").isArray());
    }

    @Test
    @DisplayName("[조회/실패] 배치 대시보드 실패")
    void getBillingDashboard_failure_shouldReturn500() throws Exception {
        // given
        given(invoiceService.getDashboard())
                .willThrow(new EntityNotFoundException(CoreErrorCode.DASHBOARD_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/billing")).andExpect(status().isInternalServerError());
    }
}

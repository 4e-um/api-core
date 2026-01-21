package com.project.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.core.controller.dto.BatchSummaryDto;
import com.project.core.controller.dto.response.BillingDashboardResponse;
import com.project.core.infra.repository.invoice.BatchFailureQueryRepository;
import com.project.core.infra.repository.invoice.BatchJobQueryRepository;
import com.project.global.config.BillingProperties;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private BatchJobQueryRepository batchJobQueryRepository;

    @Mock private BatchFailureQueryRepository batchFailureQueryRepository;

    @Mock private BillingProperties billingProperties;

    @InjectMocks private InvoiceService invoiceService;

    @Test
    @DisplayName("[조회] 성공 - 배치 대시보드 조회")
    void getDashboard_success() {
        // given
        given(batchJobQueryRepository.findLatestJobSummaries())
                .willReturn(
                        List.of(
                                new BatchSummaryDto(
                                        "invoiceJob",
                                        "COMPLETED",
                                        "COMPLETED",
                                        null,
                                        LocalDateTime.now().minusSeconds(5),
                                        LocalDateTime.now(),
                                        5000L)));

        given(batchFailureQueryRepository.findRecentFailures(10)).willReturn(List.of());

        given(billingProperties.getIframeUrl()).willReturn("https://grafana.example.com");

        given(billingProperties.getRefreshInterval()).willReturn("5s");

        // when
        BillingDashboardResponse response = invoiceService.getDashboard();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBatchJobs()).hasSize(1);
        assertThat(response.getGrafana().getIframeUrl()).isEqualTo("https://grafana.example.com");
        assertThat(response.getRecentFailures()).isEmpty();
    }

    @Test
    @DisplayName("[조회] 실패 - 배치 대시보드 없음")
    void getDashboard_repositoryFailure_shouldThrowBillingDashboardException() {
        // given
        given(batchJobQueryRepository.findLatestJobSummaries())
                .willThrow(new RuntimeException("DB error"));

        // when & then
        assertThatThrownBy(() -> invoiceService.getDashboard())
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("code")
                .isEqualTo(CoreErrorCode.DASHBOARD_NOT_FOUND);
    }
}

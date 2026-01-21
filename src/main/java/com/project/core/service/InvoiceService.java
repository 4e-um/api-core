package com.project.core.service;

import org.springframework.stereotype.Service;

import com.project.core.controller.dto.GrafanaInfoDto;
import com.project.core.controller.dto.response.BillingDashboardResponse;
import com.project.core.infra.repository.invoice.BatchFailureQueryRepository;
import com.project.core.infra.repository.invoice.BatchJobQueryRepository;
import com.project.global.config.BillingProperties;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final BatchJobQueryRepository batchJobQueryRepository;
    private final BatchFailureQueryRepository batchFailureQueryRepository;
    private final BillingProperties billingProperties;

    public BillingDashboardResponse getDashboard() {

        try {
            return BillingDashboardResponse.builder()
                    .batchJobs(batchJobQueryRepository.findLatestJobSummaries())
                    .grafana(
                            GrafanaInfoDto.builder()
                                    .iframeUrl(billingProperties.getIframeUrl())
                                    .refreshInterval(billingProperties.getRefreshInterval())
                                    .build())
                    .recentFailures(batchFailureQueryRepository.findRecentFailures(10))
                    .build();

        } catch (Exception e) {
            log.error("[BillingDashboard] 대시보드 빌딩에 실패했습니다.", e);
            throw new EntityNotFoundException(CoreErrorCode.DASHBOARD_NOT_FOUND);
        }
    }
}

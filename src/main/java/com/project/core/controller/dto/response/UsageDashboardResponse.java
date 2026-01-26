package com.project.core.controller.dto.response;

import java.util.List;

import com.project.core.controller.dto.BatchFailureLogDto;
import com.project.core.controller.dto.BatchSummaryDto;
import com.project.core.controller.dto.GrafanaInfoDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsageDashboardResponse {
    private List<BatchSummaryDto> batchJobs;
    private GrafanaInfoDto grafana;
    private List<BatchFailureLogDto> recentFailures;
}

package com.project.core.service;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.project.core.controller.dto.BatchSummaryDto;
import com.project.core.controller.dto.response.BatchScheduleResponse;
import com.project.core.util.BatchScheduleClient;
import org.springframework.scheduling.support.CronExpression;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final BatchJobQueryRepository batchJobQueryRepository;
    private final BatchFailureQueryRepository batchFailureQueryRepository;
    private final BatchScheduleClient batchScheduleClient;
    private final BillingProperties billingProperties;

    public BillingDashboardResponse getDashboard() {

        try {
            // 1️⃣ 최신 배치 실행 요약 조회
            List<BatchSummaryDto> jobs =
                    batchJobQueryRepository.findLatestJobSummaries();

            // 2️⃣ Cloud Function 스케줄 정보 결합
            List<BatchSummaryDto> enrichedJobs = jobs.stream()
                    .map(this::enrichWithSchedule)
                    .toList();

            return BillingDashboardResponse.builder()
                    .batchJobs(enrichedJobs)
                    .grafana(
                            GrafanaInfoDto.builder()
                                    .iframeUrl(billingProperties.getIframeUrl())
                                    .refreshInterval(billingProperties.getRefreshInterval())
                                    .build())
                    .recentFailures(
                            batchFailureQueryRepository.findRecentFailures(10))
                    .build();

        } catch (Exception e) {
            log.error("[BillingDashboard] 대시보드 빌딩 실패", e);
            throw new EntityNotFoundException(CoreErrorCode.DASHBOARD_NOT_FOUND);
        }
    }

    /**
     * 배치 실행 요약 + Cloud Function 스케줄 정보 결합
     */
    private BatchSummaryDto enrichWithSchedule(BatchSummaryDto job) {

        return batchScheduleClient.getSchedule(job.getJobName())
                .map(schedule -> applySchedule(job, schedule))
                .orElseGet(() ->
                        job.toBuilder()
                                .cronDescription("수동 실행")
                                .build()
                );
    }

    /**
     * cron / 설명 / 다음 실행 시각 계산 (최종)
     */
    private BatchSummaryDto applySchedule(
            BatchSummaryDto job,
            BatchScheduleResponse schedule) {

        // 1️⃣ 비활성화 스케줄
        if (!schedule.isEnabled() || schedule.getCron() == null) {
            return job.toBuilder()
                    .cronDescription("비활성화")
                    .build();
        }

        String cronStr = schedule.getCron();
        String timeZone = schedule.getTimeZone(); // ex) Asia/Seoul

        try {
            // 2️⃣ 타임존 (Cloud Function 기준)
            ZoneId zoneId = ZoneId.of(timeZone);

            // ✅ Spring용 크론 보정 (6필드)
            String springCronStr = cronStr;
            if (cronStr.split(" ").length == 5) {
                springCronStr = "0 " + cronStr;
            }

            CronExpression springCron =
                    CronExpression.parse(springCronStr);

            ZonedDateTime nextFire =
                    springCron.next(ZonedDateTime.now(zoneId));

            // ✅ cron-utils (UNIX)
            CronParser parser = new CronParser(
                    CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
            );
            Cron cron = parser.parse(cronStr);

            String cronDescription =
                    CronDescriptor.instance(Locale.KOREAN)
                            .describe(cron);

            return job.toBuilder()
                    .cronExpression(cronStr)
                    .cronDescription(cronDescription)
                    .nextFireTime(
                            nextFire != null
                                    ? nextFire.toLocalDateTime()
                                    : null)
                    .build();

        } catch (Exception e) {
            log.warn("[BillingDashboard] cron 처리 실패 job={}, cron={}",
                    job.getJobName(), cronStr, e);

            return job.toBuilder()
                    .cronExpression(cronStr)
                    .cronDescription("잘못된 크론식")
                    .build();
        }
    }
}


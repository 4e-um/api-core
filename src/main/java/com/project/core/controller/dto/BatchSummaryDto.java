package com.project.core.controller.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BatchSummaryDto {
    private String jobName;
    private String status;
    private String exitCode;
    private String exitMessage;

    private String cronExpression;        // 0 0 2 * * *
    private String cronDescription;       // 매일 오전 2시
    private LocalDateTime nextFireTime;    // 다음 실행 시각

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;
}

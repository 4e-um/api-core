package com.project.core.controller.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BatchFailureLogDto {
    private String jobName;
    private String status;
    private String exitCode;
    private String exitMessage;
    private LocalDateTime failedAt;
}

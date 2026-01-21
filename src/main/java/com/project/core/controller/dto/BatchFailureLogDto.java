package com.project.core.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BatchFailureLogDto {
    private String jobName;
    private String status;
    private String exitCode;
    private String exitMessage;
    private LocalDateTime failedAt;
}

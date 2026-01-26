package com.project.core.controller.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BatchScheduleResponse {

    private String jobType;        // INVOICE
    private String scheduleName;   // projects/.../jobs/...
    private String cron;           // 15 12 26 1 *
    private String invMonth;       // null 가능
    private String description;    // Auto-created/Updated via Batch Control API
    private String state;          // ENABLED / DISABLED
    private String timeZone;       // Asia/Seoul

    public boolean isEnabled() {
        return "ENABLED".equalsIgnoreCase(state);
    }
}

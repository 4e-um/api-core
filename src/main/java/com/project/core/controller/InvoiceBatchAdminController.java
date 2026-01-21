package com.project.core.controller;

import com.project.core.controller.dto.request.UpdateBatchScheduleRequest;
import com.project.core.service.BatchScheduleService;
import com.project.core.util.BatchTriggerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/batch/invoice")
@RequiredArgsConstructor
public class InvoiceBatchAdminController {

    private final BatchTriggerClient batchTriggerClient;
    private final BatchScheduleService scheduleService;

    @PostMapping("/run-now")
    public ResponseEntity<Void> runNow(
            @RequestParam String jobName,
            @RequestParam String invMonth
    ) {
        batchTriggerClient.trigger(jobName, invMonth);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/schedule")
    public ResponseEntity<Void> updateSchedule(
            @RequestBody UpdateBatchScheduleRequest request
    ) {
        scheduleService.updateSchedule(
                request.jobName(),
                request.cron()
        );
        batchTriggerClient.triggerSchedule(request.jobName(), request.cron());
        return ResponseEntity.ok().build();
    }
}
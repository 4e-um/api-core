package com.project.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.request.BatchTriggerRequest;
import com.project.core.controller.dto.request.UpdateBatchScheduleRequest;
import com.project.core.service.BatchScheduleService;
import com.project.core.util.BatchTriggerClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/batch/invoice")
@RequiredArgsConstructor
public class InvoiceBatchAdminController {

    private final BatchTriggerClient batchTriggerClient;
    private final BatchScheduleService scheduleService;

    @PostMapping("/run-now")
    public ResponseEntity<Void> runNow(@RequestBody BatchTriggerRequest request) {
        batchTriggerClient.trigger(request.job(), request.invMonth());
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/schedule")
    public ResponseEntity<Void> updateSchedule(@RequestBody UpdateBatchScheduleRequest request) {
        scheduleService.updateSchedule(request.job(), request.cron());
        batchTriggerClient.schedule(request.job(), request.cron());
        return ResponseEntity.ok().build();
    }
}

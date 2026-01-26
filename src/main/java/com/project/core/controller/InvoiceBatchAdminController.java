package com.project.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.request.BatchTriggerRequest;
import com.project.core.controller.dto.request.UpdateBatchScheduleRequest;
import com.project.core.util.BatchTriggerClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/batch/invoice")
@RequiredArgsConstructor
@Slf4j
public class InvoiceBatchAdminController {

    private final BatchTriggerClient batchTriggerClient;

    @PostMapping("/run-now")
    public ResponseEntity<Void> runNow(@RequestBody BatchTriggerRequest request) {
        log.info("[run-now] called. job={}, invMonth={}", request.job(), request.invMonth());
        batchTriggerClient.trigger(request.job(), request.invMonth());
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/schedule")
    public ResponseEntity<Void> updateSchedule(@RequestBody UpdateBatchScheduleRequest request) {
        batchTriggerClient.schedule(request.job(), request.cron());
        return ResponseEntity.ok().build();
    }
}

package com.project.core.service;

import org.springframework.stereotype.Service;

import com.project.core.infra.repository.invoice.BatchScheduleRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.OperationFailedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchScheduleService {

    private final BatchScheduleRepository repository;

    public void updateSchedule(String jobName, String cron) {

        try {
            repository.upsert(jobName, cron);
        } catch (Exception e) {
            log.error("[BatchSchedule] update failed. jobName={}, cron={}", jobName, cron, e);

            throw new OperationFailedException(CoreErrorCode.BATCH_SCHEDULE_UPDATE_FAILED);
        }
    }
}

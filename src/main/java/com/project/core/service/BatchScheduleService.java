package com.project.core.service;

import com.project.core.infra.repository.invoice.BatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchScheduleService {

    private final BatchScheduleRepository repository;

    public void updateSchedule(String jobName, String cron) {
        repository.upsert(jobName, cron);
    }
}
package com.project.core.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.project.core.controller.dto.request.BatchTriggerRequest;
import com.project.core.controller.dto.request.UpdateBatchScheduleRequest;
import com.project.global.config.CloudFunctionProperties;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.OperationFailedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchTriggerClient {

    private final RestTemplate restTemplate;
    private final CloudFunctionProperties properties;

    public void trigger(String job, String invMonth) {

        String url = properties.getBaseUrl() + "/run-now";
        log.info("[BatchTrigger] calling url={}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        BatchTriggerRequest body = new BatchTriggerRequest(job, invMonth);

        HttpEntity<BatchTriggerRequest> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, Void.class);

        } catch (Exception e) {
            log.error("[BatchTrigger] run-now failed. job={}, invMonth={}", job, invMonth, e);

            throw new OperationFailedException(CoreErrorCode.BATCH_TRIGGER_FAILED);
        }
    }

    public void schedule(String job, String cron) {

        String url = properties.getBaseUrl() + "/schedule";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        UpdateBatchScheduleRequest body = new UpdateBatchScheduleRequest(job, cron);

        HttpEntity<UpdateBatchScheduleRequest> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.put(url, request, Void.class);

        } catch (HttpClientErrorException e) {
            // 4xx: 요청 자체 문제 (잘못된 파라미터, validation 등)
            log.error(
                    "[BatchTrigger] schedule failed (4xx). job={}, cron={}, status={}, body={}",
                    job,
                    cron,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e);
            throw new OperationFailedException(CoreErrorCode.BATCH_TRIGGER_FAILED);

        } catch (HttpServerErrorException e) {
            // 5xx: Cloud Function 내부 오류
            log.error(
                    "[BatchTrigger] schedule failed (5xx). job={}, cron={}, status={}, body={}",
                    job,
                    cron,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e);
            throw new OperationFailedException(CoreErrorCode.BATCH_TRIGGER_FAILED);

        } catch (Exception e) {
            // 그 외 (네트워크, 직렬화 등)
            log.error("[BatchTrigger] schedule failed (unknown). job={}, cron={}", job, cron, e);
            throw new OperationFailedException(CoreErrorCode.BATCH_TRIGGER_FAILED);
        }
    }
}

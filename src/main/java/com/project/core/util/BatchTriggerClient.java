package com.project.core.util;

import com.project.global.config.CloudFunctionProperties;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.OperationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchTriggerClient {

    private final RestTemplate restTemplate;
    private final CloudFunctionProperties properties;

    public void trigger(String jobName, String invMonth) {

        String url = properties.getBaseUrl() + "/run-now";

        try {
            restTemplate.postForEntity(
                    url + "?jobName=" + jobName + "&invMonth=" + invMonth,
                    null,
                    Void.class
            );

        } catch (Exception e) {
            log.error(
                    "[BatchTrigger] run-now failed. jobName={}, invMonth={}",
                    jobName,
                    invMonth,
                    e
            );

            throw new OperationFailedException(
                    CoreErrorCode.BATCH_TRIGGER_FAILED
            );
        }
    }

    public void triggerSchedule(String jobName, String cron) {

        String url = properties.getBaseUrl() + "/schedule";

        try {
            restTemplate.postForEntity(
                    url + "?jobName=" + jobName + "&cron=" + cron,
                    null,
                    Void.class
            );

        } catch (Exception e) {
            log.error(
                    "[BatchTrigger] schedule trigger failed. jobName={}, cron={}",
                    jobName,
                    cron,
                    e
            );

            throw new OperationFailedException(
                    CoreErrorCode.BATCH_TRIGGER_FAILED
            );
        }
    }
}
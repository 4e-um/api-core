package com.project.core.util;

import com.project.global.config.CloudFunctionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class BatchTriggerClient {

    private final RestTemplate restTemplate;
    private final CloudFunctionProperties properties;

    public void trigger(String jobName, String invMonth) {

        String url = properties.getBaseUrl() + "/run-now";

        restTemplate.postForEntity(
                url + "?jobName=" + jobName + "&invMonth=" + invMonth,
                null,
                Void.class
        );
    }

    public void triggerSchedule(String jobName, String cron){

        String url = properties.getBaseUrl() + "/schedule";

        restTemplate.postForEntity(
                url + "?jobName=" + jobName + "&cron=" + cron,
                null,
                Void.class
        );
    }
}
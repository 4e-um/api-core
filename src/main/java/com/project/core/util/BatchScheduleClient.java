package com.project.core.util;

import com.project.core.controller.dto.response.BatchScheduleResponse;
import com.project.global.config.CloudFunctionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchScheduleClient {

    private final RestTemplate restTemplate;
    private final CloudFunctionProperties properties;

    public Optional<BatchScheduleResponse> getSchedule(String jobName) {

        String url = properties.getBaseUrl() + "/schedule?job=" + jobName;

        try {
            ResponseEntity<BatchScheduleResponse> response =
                    restTemplate.getForEntity(url, BatchScheduleResponse.class);

            return Optional.ofNullable(response.getBody());

        } catch (HttpStatusCodeException e) {
            log.warn("[BatchSchedule] 조회 실패 job={}, status={}, body={}",
                    jobName, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();

        } catch (Exception e) {
            log.error("[BatchSchedule] 조회 중 오류 job={}", jobName, e);
            return Optional.empty();
        }
    }
}

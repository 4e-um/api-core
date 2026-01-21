package com.project.global.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "cloud.function")
public class CloudFunctionProperties {

    /**
     * Cloud Function base URL
     * 예: https://REGION-PROJECT.cloudfunctions.net
     */
    private String baseUrl;
}
package com.project.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "cloud.function")
@Getter
@Setter
public class CloudFunctionProperties {

    /** Cloud Function base URL 예: https://REGION-PROJECT.cloudfunctions.net */
    private String baseUrl;
}

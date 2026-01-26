package com.project.global.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud.function")
@Getter @Setter
public class CloudFunctionProperties {

    /** Cloud Function base URL 예: https://REGION-PROJECT.cloudfunctions.net */
    private String baseUrl;
}

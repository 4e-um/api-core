package com.project.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ConfigurationProperties(prefix = "billing.grafana")
public class BillingProperties {
    private String iframeUrl;
    private String refreshInterval;
}

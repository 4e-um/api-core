package com.project.global.config;

import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Builder
@ConfigurationProperties(prefix = "billing.grafana")
public class BillingProperties {
    private String iframeUrl;
    private String refreshInterval;
}

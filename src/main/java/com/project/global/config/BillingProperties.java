package com.project.global.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "billing.grafana")
public class BillingProperties {
    private final String iframeUrl;
    private final String refreshInterval;
}

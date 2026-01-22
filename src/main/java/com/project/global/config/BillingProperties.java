package com.project.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@ConfigurationProperties(prefix = "billing.grafana")
public class BillingProperties {
    private final String iframeUrl;
    private final String refreshInterval;
}

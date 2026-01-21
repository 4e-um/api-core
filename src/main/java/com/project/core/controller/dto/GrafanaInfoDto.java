package com.project.core.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GrafanaInfoDto {
    private String iframeUrl;
    private String refreshInterval; // "5s"
}

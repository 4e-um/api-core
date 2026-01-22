package com.project.notification.controller.dto.request;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;

public record TemplateVersionCreateRequest(
        @NotNull Channel channel,
        String subject,
        String body,
        Map<String, Object> variables,
        TemplateStatus status) {}

package com.project.notification.controller.dto.request;

import java.util.Map;

import jakarta.validation.constraints.Pattern;

public record TemplateVersionUpdateRequest(
        @Pattern(regexp = "\\S.*") String subject,
        @Pattern(regexp = "\\S.*") String body,
        Map<String, Object> variables) {}

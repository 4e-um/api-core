package com.project.notification.controller.dto.request;

import java.util.Map;

public record TemplateVersionUpdateRequest(
        String subject, String body, Map<String, Object> variables) {}

package com.project.notification.controller.dto.request;

public record TemplateGroupUpdateRequest(
        String name,
        String description,
        Boolean isActive) {}

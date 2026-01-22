package com.project.notification.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TemplateGroupCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        Boolean isActive) {}

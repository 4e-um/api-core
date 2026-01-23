package com.project.notification.controller.dto.request;

import jakarta.validation.constraints.Pattern;

public record TemplateGroupUpdateRequest(
        @Pattern(regexp = "\\S.*") String name, String description, Boolean isActive) {}

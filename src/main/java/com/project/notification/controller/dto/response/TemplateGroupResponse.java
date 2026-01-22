package com.project.notification.controller.dto.response;

import java.time.LocalDateTime;

import com.project.notification.infra.entity.TemplateGroup;
import com.project.notification.infra.entity.TemplateVersion;

public record TemplateGroupResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean isActive,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TemplateGroupResponse from(TemplateGroup group) {
        return new TemplateGroupResponse(
                group.getId(),
                group.getCode(),
                group.getName(),
                group.getDescription(),
                group.isActive(),
                group.isDeleted(),
                group.getCreatedAt(),
                group.getUpdatedAt());
    }

    public record ActiveTemplateSummary(Long templateVersionId, int version) {
        public static ActiveTemplateSummary from(TemplateVersion version) {
            return new ActiveTemplateSummary(version.getId(), version.getVersion());
        }
    }
}

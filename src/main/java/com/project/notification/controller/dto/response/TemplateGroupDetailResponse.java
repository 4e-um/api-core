package com.project.notification.controller.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.project.notification.controller.dto.response.TemplateGroupResponse.ActiveTemplateSummary;
import com.project.notification.infra.entity.TemplateGroup;
import com.project.notification.infra.entity.enums.Channel;

public record TemplateGroupDetailResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean isActive,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<Channel, ActiveTemplateSummary> activeTemplates) {

    public static TemplateGroupDetailResponse from(
            TemplateGroup group, Map<Channel, ActiveTemplateSummary> activeTemplates) {
        return new TemplateGroupDetailResponse(
                group.getId(),
                group.getCode(),
                group.getName(),
                group.getDescription(),
                group.isActive(),
                group.isDeleted(),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                activeTemplates);
    }
}

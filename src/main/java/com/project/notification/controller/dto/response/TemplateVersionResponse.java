package com.project.notification.controller.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.project.notification.infra.entity.TemplateVersion;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;

public record TemplateVersionResponse(
        Long id,
        Long groupId,
        int version,
        Channel channel,
        String subject,
        String body,
        Map<String, Object> variables,
        TemplateStatus status,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TemplateVersionResponse from(TemplateVersion version) {
        return new TemplateVersionResponse(
                version.getId(),
                version.getTemplateGroup().getId(),
                version.getVersion(),
                version.getChannel(),
                version.getSubject(),
                version.getBody(),
                version.getVariables(),
                version.getStatus(),
                version.isDeleted(),
                version.getCreatedAt(),
                version.getUpdatedAt());
    }
}

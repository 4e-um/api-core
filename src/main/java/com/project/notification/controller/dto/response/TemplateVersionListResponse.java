package com.project.notification.controller.dto.response;

import java.time.LocalDateTime;

import com.project.notification.infra.entity.TemplateVersion;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;

public record TemplateVersionListResponse(
        Long id,
        Long groupId,
        int version,
        Channel channel,
        String subject,
        TemplateStatus status,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TemplateVersionListResponse from(TemplateVersion version) {
        return new TemplateVersionListResponse(
                version.getId(),
                version.getTemplateGroup().getId(),
                version.getVersion(),
                version.getChannel(),
                version.getSubject(),
                version.getStatus(),
                version.isDeleted(),
                version.getCreatedAt(),
                version.getUpdatedAt());
    }
}

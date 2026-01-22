package com.project.notification.controller.dto.response;

import java.time.LocalDateTime;

import com.project.notification.infra.entity.MessageLog;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.MessageStatus;

public record MessageLogResponse(
        Long id,
        String traceId,
        Long subId,
        Long templateVersionId,
        Channel channel,
        MessageStatus status,
        LocalDateTime sentAt) {
    public static MessageLogResponse from(MessageLog log) {
        return new MessageLogResponse(
                log.getId(),
                log.getTraceId(),
                log.getSubscription().getSubId(),
                (log.getTemplateVersion() != null) ? log.getTemplateVersion().getId() : null,
                log.getChannel(),
                log.getStatus(),
                log.getSentAt());
    }
}

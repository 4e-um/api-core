package com.project.notification.controller.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.project.notification.infra.entity.MessageLog;
import com.project.notification.infra.entity.TemplateVersion;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.MessageStatus;

public record MessageLogDetailResponse(
        Long id,
        String traceId,
        Long subId,
        String recipientEnc, // 암호화된 상태 그대로 전달 (필요시 마스킹 처리 로직 추가)
        Long templateVersionId,
        Channel channel,
        MessageStatus status,
        String errorMessage,
        Map<String, Object> requestPayload, // JSON 원본
        Long processingTimeMs,
        LocalDateTime sentAt,
        TemplateSnapshot snapshot) {
    public record TemplateSnapshot(
            String groupId, String groupName, Channel channel, int version) {}

    public static MessageLogDetailResponse from(MessageLog log) {
        // 템플릿 정보가 있을 경우에만 Snapshot 생성
        TemplateSnapshot snapshot = null;
        TemplateVersion templateVersion = log.getTemplateVersion();

        if (templateVersion != null) {
            snapshot =
                    new TemplateSnapshot(
                            templateVersion.getTemplateGroup().getCode(),
                            templateVersion.getTemplateGroup().getName(),
                            templateVersion.getChannel(),
                            templateVersion.getVersion());
        }

        return new MessageLogDetailResponse(
                log.getId(),
                log.getTraceId(),
                log.getSubscription().getSubId(),
                log.getRecipientEnc(),
                (templateVersion != null) ? templateVersion.getId() : null,
                log.getChannel(),
                log.getStatus(),
                log.getErrorMessage(),
                log.getRequestPayload(),
                log.getProcessingTimeMs(),
                log.getSentAt(),
                snapshot);
    }
}

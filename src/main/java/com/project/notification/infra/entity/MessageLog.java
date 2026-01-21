package com.project.notification.infra.entity;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.MessageStatus;

import io.hypersistence.utils.hibernate.type.json.JsonType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "message_log",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "idx_message_log_trace",
                    columnNames = {"trace_id", "channel"})
        },
        indexes = {
            @Index(name = "idx_message_log_sub", columnList = "sub_id"),
            @Index(name = "idx_message_log_sent_at", columnList = "sent_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id", nullable = false, length = 36)
    private String traceId; // Kafka 추적 ID, 멱등성 보장 키

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "sub_id", nullable = false)
    private Subscription subscription;

    @Column(name = "recipient_enc", length = 500)
    private String recipientEnc; // 수신자 정보 암호화

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_version_id")
    private TemplateVersion templateVersion; // 사용된 템플릿 버전 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 10)
    private Channel channel; // 채널 (EMAIL, SMS)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status; // 발송 상태

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // 에러 메시지

    @Type(JsonType.class)
    @Column(name = "request_payload", columnDefinition = "jsonb")
    private Map<String, Object> requestPayload; // 카프카 메시지 원본 (JSONB)

    @Column(name = "processing_time_ms")
    private Long processingTimeMs; // 처리 시간

    @Column(name = "sent_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime sentAt; // 발송 일시

    @Builder
    public MessageLog(
            Long id,
            String traceId,
            Subscription subscription,
            String recipientEnc,
            TemplateVersion templateVersion,
            Channel channel,
            MessageStatus status,
            String errorMessage,
            Map<String, Object> requestPayload,
            Long processingTimeMs,
            LocalDateTime sentAt) {
        this.id = id;
        this.traceId = traceId;
        this.subscription = subscription;
        this.recipientEnc = recipientEnc;
        this.templateVersion = templateVersion;
        this.channel = channel;
        this.status = status;
        this.errorMessage = errorMessage;
        this.requestPayload = requestPayload;
        this.processingTimeMs = processingTimeMs;
        this.sentAt = sentAt;
    }
}

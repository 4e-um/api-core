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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;

import io.hypersistence.utils.hibernate.type.json.JsonType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "template_version",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_tpl_group_ver_channel",
                    columnNames = {"group_id", "channel", "version"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TemplateGroup templateGroup;

    @Column(nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Channel channel;

    @Column(length = 200)
    private String subject; // Email Only

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> variables;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_status", nullable = false, length = 20)
    private TemplateStatus status;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Builder 패턴 권장
    @Builder
    public TemplateVersion(
            TemplateGroup templateGroup,
            int version,
            Channel channel,
            String subject,
            String body,
            Map<String, Object> variables) {
        this.templateGroup = templateGroup;
        this.version = version;
        this.channel = channel;
        this.subject = subject;
        this.body = body;
        this.variables = variables;
        this.status = TemplateStatus.DRAFT; // 기본값 DRAFT
    }

    public void updateContent(String subject, String body, Map<String, Object> variables) {
        if (subject != null) {
            this.subject = subject;
        }
        if (body != null) {
            this.body = body;
        }
        if (variables != null) {
            this.variables = variables;
        }
    }

    public void activate() {
        this.status = TemplateStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = TemplateStatus.DRAFT;
    }

    public void delete() {
        this.isDeleted = true;
    }
}

package com.project.notification.infra.entity;

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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

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
    @Column(nullable = false)
    private Channel channel;

    private String subject; // Email Only

    @Lob
    @Column(nullable = false)
    private String body;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> variables;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_status", nullable = false)
    private TemplateStatus status;

    @Column(nullable = false)
    private boolean isDeleted = false;

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

    public void activate() {
        this.status = TemplateStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = TemplateStatus.DRAFT;
    }

    public void delete() {
        this.isDeleted = true;
    }

    // 내용 수정 메서드 등...
}

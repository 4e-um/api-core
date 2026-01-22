package com.project.notification.infra.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // 업무 코드 (BILLING_NOTICE 등)

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성자, 비즈니스 메서드 (update, delete 등)
    @Builder
    private TemplateGroup(String code, String name, String description, boolean isActive) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.isDeleted = false;
    }

    // 생성 후 비즈니스 메서드 (update, delete 등)
    public void update(String name, String description, Boolean isActive) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public void delete() {
        this.isDeleted = true;
        this.isActive = false;
    }
}

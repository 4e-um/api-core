package com.project.notification.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// AuditingEntityListener 등을 사용 중이라면 extends BaseEntity
public class TemplateGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // 업무 코드 (BILLING_NOTICE 등)

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean isDeleted = false;

    // 생성자, 비즈니스 메서드 (update, delete 등)
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
    }
}

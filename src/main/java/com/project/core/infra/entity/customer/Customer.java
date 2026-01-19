package com.project.core.infra.entity.customer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.project.core.infra.entity.customer.enums.Grade;
import com.project.core.infra.entity.subscription.Subscription;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contact_enc", nullable = false) // 암호화된 전화번호
    private String contactEnc;

    @Column(name = "contact_hash", nullable = false) // 조회용 암호
    private String contactHash;

    @Column(name = "email_enc", nullable = false) // 암호화된 이메일
    private String emailEnc;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false, length = 20)
    private Grade grade;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Subscription> subscriptionHistory = new ArrayList<>();

    @Builder
    private Customer(
            String name, String contactEnc, String emailEnc, Grade grade, String contactHash) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }
        if (contactEnc == null || contactEnc.isBlank()) {
            throw new IllegalArgumentException("contactEnc는 필수입니다.");
        }
        if (emailEnc == null || emailEnc.isBlank()) {
            throw new IllegalArgumentException("emailEnc는 필수입니다.");
        }
        if (grade == null) {
            throw new IllegalArgumentException("grade는 필수입니다.");
        }
        if (contactHash == null) {
            throw new IllegalArgumentException("contactHash는 필수입니다.");
        }

        this.name = name;
        this.contactEnc = contactEnc;
        this.contactHash = contactHash;

        this.emailEnc = emailEnc;
        this.grade = grade;

        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
        this.subscriptionHistory = new ArrayList<>();
    }

    public void changeEmailEnc(String emailEnc) {
        this.emailEnc = emailEnc;
    }

    public void changeGrade(Grade grade) {
        this.grade = grade;
    }
}

package com.project.core.infra.entity.plan;

import java.time.LocalDateTime;

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

import com.project.core.infra.entity.plan.enums.AllotmentPeriod;
import com.project.core.infra.entity.subscription.Subscription;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscription_plan")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sp_id")
    private Long spId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "plan_name", nullable = false, length = 30)
    private String planName;

    @Column(name = "cost", nullable = false)
    private Integer cost;

    @Column(name = "allotment_amount", nullable = false)
    private Long allotmentAmount; // MB 단위, -1은 무제한

    @Enumerated(EnumType.STRING)
    @Column(name = "allotment_period", nullable = false, length = 10)
    private AllotmentPeriod allotmentPeriod; // MONTH / DAY

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "left_date", nullable = false)
    private LocalDateTime leftDate;

    @Builder
    public SubscriptionPlan(Subscription subscription, Plan plan) {
        this.subscription = subscription;
        this.plan = plan;
        this.planName = plan.getPlanName();
        this.cost = plan.getPlanBaseFee(); // 요금제 가격을 스냅샷으로 저장
        this.allotmentAmount = plan.getAllotmentAmount();
        this.allotmentPeriod = plan.getAllotmentPeriod();
        this.createdDate = LocalDateTime.now();
        this.leftDate = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
    }

    // 요금제 해지(만료) 처리 메소드
    public void expire() {
        this.leftDate = LocalDateTime.now();
    }
}

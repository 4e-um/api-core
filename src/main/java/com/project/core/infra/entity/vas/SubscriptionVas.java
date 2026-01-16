package com.project.core.infra.entity.vas;

import java.time.Clock;
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

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.vas.enums.VasStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscription_vas")
public class SubscriptionVas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sv_id")
    private Long svId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vas_id", nullable = false)
    private Vas vas;

    @Column(name = "monthly_fee", nullable = false)
    private Integer monthlyFee;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private VasStatus status;

    @Builder
    public SubscriptionVas(Subscription subscription, Vas vas, Clock clock) {
        this.subscription = subscription;
        this.vas = vas;
        this.monthlyFee = vas.getMonthlyFee();
        this.startDate = LocalDateTime.now(clock);
        this.status = VasStatus.ACTIVE;
    }

    public void terminate(Clock clock) {
        this.status = VasStatus.TERMINATED;
        this.endDate = LocalDateTime.now(clock);
    }
}

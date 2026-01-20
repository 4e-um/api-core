package com.project.core.infra.entity.subscription;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.micro.MicroPayment;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.entity.vas.SubscriptionVas;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscription")
public class Subscription {

    private static final int DEFAULT_SEND_DAY = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_id")
    private Long subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "phone_hash", nullable = false)
    private String phoneHash;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SubscriptionStatus status;

    @Column(name = "send_day", nullable = false)
    private Integer sendDay;

    // ---------------------------------------------------------------------

    // 요금제 이력 (1:N)
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<SubscriptionPlan> planHistory = new ArrayList<>();

    // 부가서비스 이력 (1:N)
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<SubscriptionVas> vasHistory = new ArrayList<>();

    // 할인 이력 (1:N)
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<SubscriptionDiscount> discountHistory = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<MicroPayment> microPaymentHistory = new ArrayList<>();

    @Builder
    public Subscription(Customer customer, String phoneNumber, String phoneHash, Clock clock) {
        this.customer = customer;
        this.phoneNumber = phoneNumber;
        this.phoneHash = phoneHash;
        this.startDate = LocalDateTime.now(clock);
        this.status = SubscriptionStatus.ACTIVE;
        this.sendDay = DEFAULT_SEND_DAY;
    }

    // 서비스 해지 처리 메서드
    public void terminate(Clock clock) {
        this.status = SubscriptionStatus.TERMINATED;
        this.endDate = LocalDateTime.now(clock);
    }
}

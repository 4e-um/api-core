package com.project.core.infra.entity.discount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.Status;
import com.project.core.infra.entity.discount.enums.TargetScope;
import com.project.core.infra.entity.subscription.Subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscription_discount")
public class SubscriptionDiscount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sd_id")
	private Long sdId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discount_id", nullable = false)
	private DiscountPolicy discountPolicy;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sub_id", nullable = false)
	private Subscription subscription;
	
	@Column(name = "discount_type", nullable = false)
	private DiscountType discountType;
	
	@Column(name = "value", nullable = false)
	private BigDecimal value;
	
	@Column(name = "target_scope", nullable = false)
	private TargetScope targetScope;
	
	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;
	
	@Column(name = "status", nullable = false)
	private Status status;
	
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	public void setStatusTerminated() {
		this.status = Status.TERMINATED;
	}
	
	@Builder
    private SubscriptionDiscount(
            DiscountPolicy discountPolicy,
            Subscription subscription,
            DiscountType discountType,
            BigDecimal value,
            TargetScope targetScope,
            LocalDateTime startDate
    ) {
        if (discountPolicy == null) throw new IllegalArgumentException("discountPolicy는 필수입니다.");
        if (subscription == null) throw new IllegalArgumentException("subscription은 필수입니다.");
        if (discountType == null) throw new IllegalArgumentException("discountType는 필수입니다.");
        if (value == null) throw new IllegalArgumentException("value는 필수입니다.");
        if (targetScope == null) throw new IllegalArgumentException("targetScope는 필수입니다.");

        this.discountPolicy = discountPolicy;
        this.subscription = subscription;
        this.discountType = discountType;
        this.value = value;
        this.targetScope = targetScope;
        this.startDate = startDate != null ? startDate : LocalDateTime.now();

        // 기본값
        this.endDate = null;
        this.status = Status.ACTIVE;
    }
}

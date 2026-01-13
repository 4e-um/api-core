package com.project.core.infra.entity.subscription;

import com.project.core.infra.entity.plan.Plan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

	@Column(name = "cost", nullable = false)
	private Integer cost;

	@Column(name = "created_date", nullable = false)
	private LocalDateTime createdDate;

	@Column(name = "left_date", nullable = false)
	private LocalDateTime leftDate;

	@Builder
	public SubscriptionPlan(Subscription subscription, Plan plan) {
		this.subscription = subscription;
		this.plan = plan;
		this.cost = plan.getPlanBaseFee(); // 요금제 가격을 스냅샷으로 저장
		this.createdDate = LocalDateTime.now();
		this.leftDate = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
	}

	// 요금제 해지(만료) 처리 메소드
	public void expire() {
		this.leftDate = LocalDateTime.now();
	}
}

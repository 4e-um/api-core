package com.project.core.infra.entity.subscription;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.plan.SubscriptionPlan;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.entity.vas.SubscriptionVas;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscription")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sub_id")
	private Long subId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Column(name = "phone_number", nullable = false)
	private String phoneNumber;

	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 10)
	private SubscriptionStatus status;
	
//---------------------------------------------------------------------
	
	// 요금제 이력 (1:N)
	@OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
	private List<SubscriptionPlan> planHistory = new ArrayList<>();

	// 부가서비스 이력 (1:N)
	@OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
	private List<SubscriptionVas> vasHistory = new ArrayList<>();

	// 할인 이력 (1:N)
}

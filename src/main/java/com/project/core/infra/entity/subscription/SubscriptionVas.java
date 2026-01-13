package com.project.core.infra.entity.subscription;

import com.project.core.infra.entity.customer.Customer;

import jakarta.persistence.*;

@Entity
public class SubscriptionVas {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sv_id")
	private Long svId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sub_id", nullable = false)
	private Subscription subscription;
}

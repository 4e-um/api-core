package com.project.core.infra.entity;

import jakarta.persistence.*;

@Entity
public class SubscriptionPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sp_id")
	private Long spId;
}

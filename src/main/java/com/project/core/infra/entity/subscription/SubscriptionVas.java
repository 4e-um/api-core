package com.project.core.infra.entity.subscription;

import jakarta.persistence.*;

@Entity
public class SubscriptionVas {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sv_id")
	private Long svId;
}

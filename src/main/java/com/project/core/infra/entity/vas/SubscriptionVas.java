package com.project.core.infra.entity.vas;

import jakarta.persistence.*;

@Entity
public class SubscriptionVas {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sv_id")
	private Long svId;
}

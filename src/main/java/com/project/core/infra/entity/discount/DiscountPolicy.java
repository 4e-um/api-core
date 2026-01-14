package com.project.core.infra.entity.discount;

import java.util.ArrayList;
import java.util.List;

import com.project.core.infra.entity.discount.enums.Active;
import com.project.core.infra.entity.discount.enums.Category;
import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.TargetScope;
import com.project.core.infra.entity.vas.SubscriptionVas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "discount_policy")
public class DiscountPolicy {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "discount_id")
	private Long discountId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "discount_type", nullable = false)
	private DiscountType discountType;
	
	@Column(name = "value", nullable = false)
	private Double value;
	
	@Column(name = "category", nullable = false)
	private Category category;
	
	@Column(name = "target_scope", nullable = false)
	private TargetScope targetScope;
	
	@Column(name = "active", nullable = false)
	private Active active;
//------------------------------------------------------------------
	@OneToMany(mappedBy = "discount_policy", cascade = CascadeType.ALL)
	private List<SubscriptionDiscount> discountHistory = new ArrayList<>();
}

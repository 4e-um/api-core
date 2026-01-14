package com.project.core.infra.repository.discount;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.discount.SubscriptionDiscount;

public interface SubscriptionDiscountRepository extends JpaRepository<SubscriptionDiscount, Long> {
	List<SubscriptionDiscount> findBySubId(Long subId);
	Optional<SubscriptionDiscount> findBySdId(Long sdId);
}

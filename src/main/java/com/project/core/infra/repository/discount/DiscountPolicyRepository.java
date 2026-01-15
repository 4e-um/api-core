package com.project.core.infra.repository.discount;

import com.project.core.infra.entity.discount.DiscountPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {}

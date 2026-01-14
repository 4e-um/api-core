package com.project.core.infra.repository.discount;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.discount.DiscountPolicy;

public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {

}

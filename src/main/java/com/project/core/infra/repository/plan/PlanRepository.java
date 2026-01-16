package com.project.core.infra.repository.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.plan.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {}

package com.project.core.infra.repository.plan;

import com.project.core.infra.entity.plan.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}

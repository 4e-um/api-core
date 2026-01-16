package com.project.core.infra.repository.plan;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.core.infra.entity.plan.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    // 해당 회선이 현재 사용 중인 요금제 조회 (leftDate가 현재보다 미래인 것)
    @Query(
            "SELECT sp from SubscriptionPlan sp "
                    + "WHERE sp.subscription.subId = :subId AND sp.leftDate > CURRENT_TIMESTAMP")
    Optional<SubscriptionPlan> findActivePlanBySubId(@Param("subId") Long subId);
}

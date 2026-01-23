package com.project.core.infra.repository.usage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.core.infra.entity.usage.UsageSummaryMonthly;

public interface UsageSummaryMonthlyRepository extends JpaRepository<UsageSummaryMonthly, Long> {

    @Query(
            """
    SELECT u
    FROM UsageSummaryMonthly u
    WHERE u.id.subId = :subId
      AND u.id.period = :period
    """)
    Optional<UsageSummaryMonthly> findBySubIdAndPeriod(
            @Param("subId") Long subId, @Param("period") String period);
}

package com.project.core.infra.repository.usage;

import com.project.core.infra.entity.usage.UsageSummaryDaily;
import com.project.core.infra.entity.usage.UsageSummaryMonthly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsageSummaryMonthlyRepository extends JpaRepository<UsageSummaryMonthly, Long> {

    @Query("""
    SELECT u
    FROM UsageSummaryMonthly u
    WHERE u.id.subId = :subId
      AND u.id.period = :period
    """)
    Optional<UsageSummaryMonthly> findBySubIdAndPeriod(
            @Param("subId") Long subId,
            @Param("period") String period
    );
}

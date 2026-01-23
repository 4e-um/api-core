package com.project.core.infra.repository.usage;

import com.project.core.infra.entity.usage.UsageSummaryDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsageSummaryDailyRepository extends JpaRepository<UsageSummaryDaily,Long> {

    @Query("""
    SELECT u
    FROM UsageSummaryDaily u
    WHERE u.id.subId = :subId
      AND u.id.usageDate = :usageDate
    """)
    Optional<UsageSummaryDaily> findBySubIdAndUsageDate(
            @Param("subId") Long subId,
            @Param("usageDate") String usageDate
    );
}

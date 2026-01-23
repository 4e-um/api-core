package com.project.core.infra.entity.usage;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_summary_monthly")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageSummaryMonthly {

    @EmbeddedId private UsageSummaryMonthlyId id;

    @Column(name = "total_used_bytes", nullable = false)
    private Long totalUsedBytes;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

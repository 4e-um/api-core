package com.project.core.infra.entity.usage;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageSummaryMonthlyId implements Serializable {

    private Long subId;

    private String period;

    @Override
    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof UsageSummaryMonthlyId)) {
            return false;
        }
        UsageSummaryMonthlyId that = (UsageSummaryMonthlyId) ob;
        return Objects.equals(subId, that.subId) && Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subId, period);
    }
}

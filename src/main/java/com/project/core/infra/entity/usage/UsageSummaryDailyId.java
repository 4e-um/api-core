package com.project.core.infra.entity.usage;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageSummaryDailyId implements Serializable {

    private Long subId;

    private String usageDate;

    @Override
    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof UsageSummaryDailyId)) {
            return false;
        }
        UsageSummaryDailyId that = (UsageSummaryDailyId) ob;
        return Objects.equals(subId, that.subId) && Objects.equals(usageDate, that.usageDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subId, usageDate);
    }
}

package com.project.core.controller.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.Status;
import com.project.core.infra.entity.discount.enums.TargetScope;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionDiscountResponse {

    private Long sdId;
    private Long subId;

    private Long discountId;
    private DiscountType discountType;
    private BigDecimal value;
    private TargetScope targetScope;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Status status;

    public static SubscriptionDiscountResponse from(SubscriptionDiscount sd) {

        return SubscriptionDiscountResponse.builder()
                .sdId(sd.getSdId())
                .subId(sd.getSubscription().getSubId())
                .discountId(sd.getDiscountPolicy().getDiscountId())
                .discountType(sd.getDiscountType())
                .value(sd.getValue())
                .targetScope(sd.getTargetScope())
                .startDate(sd.getStartDate())
                .endDate(sd.getEndDate())
                .status(sd.getStatus())
                .build();
    }
}

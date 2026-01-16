package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

public record SubscriptionResponse(
        Long subId,
        String maskedPhoneNumber,
        LocalDateTime startDate,
        LocalDateTime endDate,
        SubscriptionStatus status,
        Integer sendDay) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getSubId(),
                MaskingUtil.maskPhone(subscription.getPhoneNumber()),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus(),
                subscription.getSendDay());
    }
}

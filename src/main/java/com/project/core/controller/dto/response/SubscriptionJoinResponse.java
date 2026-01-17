package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

public record SubscriptionJoinResponse(
        Long subId,
        Long customerId,
        Long planId,
        String phoneNumber,
        SubscriptionStatus status,
        LocalDateTime startDate) {}

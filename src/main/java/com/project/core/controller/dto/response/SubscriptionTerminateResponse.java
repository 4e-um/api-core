package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

public record SubscriptionTerminateResponse(
        Long subId, SubscriptionStatus status, LocalDateTime terminatedAt) {}

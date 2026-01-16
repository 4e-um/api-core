package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionTerminateResponse {
    private Long subId;
    private SubscriptionStatus status;
    private LocalDateTime terminatedAt;
}

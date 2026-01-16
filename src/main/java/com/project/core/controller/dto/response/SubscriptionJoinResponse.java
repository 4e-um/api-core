package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionJoinResponse {
    private Long subId;
    private Long customerId;
    private Long planId;
    private String phoneNumber;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
}

package com.project.core.controller.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

public class PlanDto {

    public record JoinRequest(@NotNull Long customerId, @NotNull Long planId) {}

    public record ChangeRequest(@NotNull Long planId) {}

    public record JoinResponse(
            Long subId,
            Long customerId,
            Long planId,
            String phoneNumber,
            SubscriptionStatus status,
            LocalDateTime startDate) {}

    public record ChangeResponse(
            Long subId, Long oldPlanId, Long newPlanId, LocalDateTime changedAt) {}

    public record TerminateResponse(
            Long subId, SubscriptionStatus status, LocalDateTime terminatedAt) {}

    public record HistoryResponse(
            Long spId,
            String planName,
            Integer cost,
            LocalDateTime joinDate,
            LocalDateTime leftDate) {}
}

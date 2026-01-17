package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

public record PlanChangeResponse(
        Long subId, Long oldPlanId, Long newPlanId, LocalDateTime changedAt) {}

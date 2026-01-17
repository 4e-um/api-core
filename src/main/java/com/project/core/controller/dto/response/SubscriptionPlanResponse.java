package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

public record SubscriptionPlanResponse(
        Long spId, String planName, Integer cost, LocalDateTime joinDate, LocalDateTime leftDate) {}

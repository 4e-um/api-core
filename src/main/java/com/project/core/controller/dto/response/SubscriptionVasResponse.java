package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

public record SubscriptionVasResponse(
        Long svId,
        String vasName,
        Integer fee,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate) {}

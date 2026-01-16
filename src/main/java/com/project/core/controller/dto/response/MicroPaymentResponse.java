package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

public record MicroPaymentResponse(
        Long microId,
        Long subId,
        String name,
        Integer amount,
        LocalDateTime payDate,
        String status) {}

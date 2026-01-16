package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

public record MicroPaymentHistoryResponse(
    Long microId, String name, Integer amount, LocalDateTime payDate, String status) {}

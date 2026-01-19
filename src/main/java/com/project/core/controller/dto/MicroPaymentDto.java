package com.project.core.controller.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MicroPaymentDto {

    public record Request(@NotBlank String name, @NotNull @Positive Integer amount) {}

    public record Response(
            Long microId,
            Long subId,
            String name,
            Integer amount,
            LocalDateTime payDate,
            String status) {}

    public record HistoryResponse(
            Long microId, String name, Integer amount, LocalDateTime payDate, String status) {}
}

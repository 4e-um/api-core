package com.project.core.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public class VasDto {

    public record Request(@NotNull Long vasId) {}

    public record BulkRequest(@NotNull List<Long> vasIds) {}

    public record JoinResponse(
            Long subVasId, Long subId, Long vasId, String status, LocalDateTime startDate) {}

    public record TerminateResponse(
            Long subVasId, Long subId, Long vasId, String status, LocalDateTime endDate) {}

    public record BulkTerminateResponse(Long subId, int count, List<TerminateResponse> responses) {}

    public record HistoryResponse(
            Long svId,
            String vasName,
            Integer fee,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate) {}
}

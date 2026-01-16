package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

public record VasJoinResponse(
    Long subVasId, Long subId, Long vasId, String status, LocalDateTime startDate) {}

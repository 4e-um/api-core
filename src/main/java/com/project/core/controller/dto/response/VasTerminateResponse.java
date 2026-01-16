package com.project.core.controller.dto.response;

import java.time.LocalDateTime;

public record VasTerminateResponse(
        Long subVasId, Long subId, Long vasId, String status, LocalDateTime endDate) {}

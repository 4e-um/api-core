package com.project.core.controller.dto.response;

import java.util.List;

public record VasBulkTerminateResponse(
        Long subId, int count, List<VasTerminateResponse> terminatedList) {}

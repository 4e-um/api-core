package com.project.core.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record MicroPaymentCancelRequest(@NotNull Long subId) {}

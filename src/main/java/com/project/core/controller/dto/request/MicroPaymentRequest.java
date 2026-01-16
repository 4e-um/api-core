package com.project.core.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MicroPaymentRequest(
    @NotNull Long subId, @NotBlank String name, @NotNull @Positive Integer amount) {}

package com.project.core.controller.dto.request;

import com.project.core.infra.entity.customer.enums.Grade;

public record ChangeGradeRequest(Grade grade) {}

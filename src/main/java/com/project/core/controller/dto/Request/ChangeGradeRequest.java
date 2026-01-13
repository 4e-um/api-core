package com.project.core.controller.dto.Request;

import com.project.core.infra.entity.customer.enums.Grade;

public record ChangeGradeRequest(Grade grade) {

}

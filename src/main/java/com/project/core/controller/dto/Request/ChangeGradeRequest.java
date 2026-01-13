package com.project.core.controller.dto.Request;

import com.project.core.infra.entity.user.enums.Grade;

public record ChangeGradeRequest(Grade grade) {

}

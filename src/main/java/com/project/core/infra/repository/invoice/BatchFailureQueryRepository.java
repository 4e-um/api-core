package com.project.core.infra.repository.invoice;

import java.util.List;

import com.project.core.controller.dto.BatchFailureLogDto;

public interface BatchFailureQueryRepository {
    List<BatchFailureLogDto> findRecentFailures(int limit);
}

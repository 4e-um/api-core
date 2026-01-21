package com.project.core.infra.repository.invoice;

import com.project.core.controller.dto.BatchFailureLogDto;

import java.util.List;

public interface BatchFailureQueryRepository {
    List<BatchFailureLogDto> findRecentFailures(int limit);
}

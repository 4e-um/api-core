package com.project.core.infra.repository.usage;

import java.util.List;

import com.project.core.controller.dto.BatchFailureLogDto;

public interface UsageBatchFailureQueryRepository {
    List<BatchFailureLogDto> findRecentFailures(int limit);
}

package com.project.core.infra.repository.usage;

import java.util.List;

import com.project.core.controller.dto.BatchSummaryDto;

public interface UsageBatchJobQueryRepository {
    List<BatchSummaryDto> findLatestJobSummaries();
}

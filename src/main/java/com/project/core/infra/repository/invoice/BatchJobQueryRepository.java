package com.project.core.infra.repository.invoice;

import java.util.List;

import com.project.core.controller.dto.BatchSummaryDto;

public interface BatchJobQueryRepository {
    List<BatchSummaryDto> findLatestJobSummaries();
}

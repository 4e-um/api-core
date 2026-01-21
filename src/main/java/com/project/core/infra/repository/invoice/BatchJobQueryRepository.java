package com.project.core.infra.repository.invoice;

import com.project.core.controller.dto.BatchSummaryDto;

import java.util.List;

public interface BatchJobQueryRepository {
    List<BatchSummaryDto> findLatestJobSummaries();

}

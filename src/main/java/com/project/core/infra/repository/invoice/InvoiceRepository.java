package com.project.core.infra.repository.invoice;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InvoiceRepository {

    private final JdbcTemplate jdbcTemplate;

    //    public BatchSummaryDto selectExecutionSummary() {
    //
    //    }

    //    public List<BatchJobDetailDto> selectLatestJobExecution() {
    //
    //    }
}

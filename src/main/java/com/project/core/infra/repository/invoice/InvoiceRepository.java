package com.project.core.infra.repository.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


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

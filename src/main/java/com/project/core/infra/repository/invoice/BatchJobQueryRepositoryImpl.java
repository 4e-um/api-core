package com.project.core.infra.repository.invoice;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.project.core.controller.dto.BatchSummaryDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BatchJobQueryRepositoryImpl implements BatchJobQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<BatchSummaryDto> findLatestJobSummaries() {

        String sql =
                """
                            SELECT
                                ji.JOB_NAME,
                                je.STATUS,
                                je.EXIT_CODE,
                                je.EXIT_MESSAGE,
                                je.START_TIME,
                                je.END_TIME,
                                EXTRACT(EPOCH FROM (je.END_TIME - je.START_TIME)) * 1000 AS DURATION_MS
                            FROM BATCH_JOB_INSTANCE ji
                                     JOIN BATCH_JOB_EXECUTION je
                                          ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
                            WHERE ji.JOB_NAME IN ('invoiceJob', 'invoiceItemJob')
                              AND je.JOB_EXECUTION_ID IN (
                                SELECT MAX(je2.JOB_EXECUTION_ID)
                                FROM BATCH_JOB_EXECUTION je2
                                         JOIN BATCH_JOB_INSTANCE ji2
                                              ON ji2.JOB_INSTANCE_ID = je2.JOB_INSTANCE_ID
                                WHERE ji2.JOB_NAME IN ('invoiceJob', 'invoiceItemJob')
                                GROUP BY ji2.JOB_NAME
                            )
                            ORDER BY ji.JOB_NAME
                        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    var startTimeStamp = rs.getTimestamp("START_TIME");
                    var endTimeStamp = rs.getTimestamp("END_TIME");
                    return new BatchSummaryDto(
                            rs.getString("JOB_NAME"),
                            rs.getString("STATUS"),
                            rs.getString("EXIT_CODE"),
                            rs.getString("EXIT_MESSAGE"),
                            startTimeStamp != null ? startTimeStamp.toLocalDateTime() : null,
                            endTimeStamp != null ? endTimeStamp.toLocalDateTime() : null,
                            rs.getLong("DURATION_MS"));
                });
    }
}

package com.project.core.infra.repository.usage;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.project.core.controller.dto.BatchFailureLogDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UsageBatchFailureQueryRepositoryImpl implements UsageBatchFailureQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<BatchFailureLogDto> findRecentFailures(int limit) {

        String sql =
                """
                    SELECT
                        ji.JOB_NAME,
                        je.STATUS,
                        je.EXIT_CODE,
                        je.EXIT_MESSAGE,
                        je.END_TIME
                    FROM BATCH_JOB_INSTANCE ji
                    JOIN BATCH_JOB_EXECUTION je
                      ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
                    WHERE je.STATUS = 'FAILED'
                      AND ji.JOB_NAME IN ('usageAggregationJob', 'usageNotificationJob')
                    ORDER BY je.END_TIME DESC
                    LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                ps -> ps.setInt(1, limit),
                (rs, rowNum) ->
                        new BatchFailureLogDto(
                                rs.getString("JOB_NAME"),
                                rs.getString("STATUS"),
                                rs.getString("EXIT_CODE"),
                                rs.getString("EXIT_MESSAGE"),
                                rs.getTimestamp("END_TIME").toLocalDateTime()));
    }
}

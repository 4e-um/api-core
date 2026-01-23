package com.project.core.infra.repository.invoice;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BatchScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    public void upsert(String jobName, String cron) {

        String sql = """
                    INSERT INTO batch_schedule (job_name, cron_expression, updated_at)
                    VALUES (?, ?, now())
                    ON CONFLICT (job_name)
                    DO UPDATE SET
                        cron_expression = EXCLUDED.cron_expression,
                        updated_at = now()
                """;

        jdbcTemplate.update(sql, jobName, cron);
    }
}

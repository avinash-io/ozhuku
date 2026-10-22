package io.github.avinashio.ozhuku.server.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseHealthIndicatorTest {

    @Test
    void shouldReportHealthyWhenDatabaseResponds() {
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(1);

        final DatabaseHealthIndicator indicator =
                new DatabaseHealthIndicator(jdbcTemplate);

        final Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("available",
                health.getDetails().get("database"));
    }

    @Test
    void shouldReportUnhealthyWhenDatabaseFails() {
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new DataAccessException(
                        "Connection refused") {
                    private static final long serialVersionUID = 1L;
                });

        final DatabaseHealthIndicator indicator =
                new DatabaseHealthIndicator(jdbcTemplate);

        final Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("unavailable",
                health.getDetails().get("database"));
    }
}
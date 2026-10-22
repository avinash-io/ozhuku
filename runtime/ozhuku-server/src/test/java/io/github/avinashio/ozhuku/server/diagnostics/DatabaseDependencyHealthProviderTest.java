package io.github.avinashio.ozhuku.server.diagnostics;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseDependencyHealthProviderTest {

    @Test
    void shouldReportHealthyWhenDatabaseResponds() {
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(1);

        final DatabaseDependencyHealthProvider provider =
                new DatabaseDependencyHealthProvider(jdbcTemplate);

        final DependencyHealth health = provider.check();

        assertEquals("DATABASE", health.scope());
        assertEquals("HEALTHY", health.status());
        assertTrue(health.detail().contains("healthy"));
    }

    @Test
    void shouldReportUnhealthyWhenDatabaseFails() {
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new DataAccessException("Connection refused") {
                    private static final long serialVersionUID = 1L;
                });

        final DatabaseDependencyHealthProvider provider =
                new DatabaseDependencyHealthProvider(jdbcTemplate);

        final DependencyHealth health = provider.check();

        assertEquals("DATABASE", health.scope());
        assertEquals("UNHEALTHY", health.status());
        assertTrue(health.detail().contains("Connection refused"));
    }
}
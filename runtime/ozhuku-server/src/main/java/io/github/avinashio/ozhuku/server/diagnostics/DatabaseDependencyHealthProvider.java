package io.github.avinashio.ozhuku.server.diagnostics;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Dependency health provider for the control-plane database.
 *
 * <p>Performs a targeted health check by executing a trivial query.
 * This is an operator-triggered diagnostic, not an automatic health
 * check, so it does not affect liveness.</p>
 */
@Component
public class DatabaseDependencyHealthProvider
        implements DependencyHealthProvider {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a database dependency health provider.
     *
     * @param jdbcTemplate the JDBC template
     */
    public DatabaseDependencyHealthProvider(
            final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DependencyHealth check() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return DependencyHealth.healthy(
                    "DATABASE",
                    "Database connection is healthy");
        } catch (final DataAccessException exception) {
            return DependencyHealth.unhealthy(
                    "DATABASE",
                    "Database connection failed: "
                            + exception.getMessage());
        }
    }
}

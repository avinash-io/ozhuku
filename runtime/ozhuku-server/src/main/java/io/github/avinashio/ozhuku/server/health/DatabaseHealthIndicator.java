package io.github.avinashio.ozhuku.server.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health indicator for the control-plane database.
 *
 * <p>Checks that the database is reachable and responsive by executing
 * a trivial query. This contributes to the readiness health group but
 * not the liveness group.</p>
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a database health indicator.
     *
     * @param jdbcTemplate the JDBC template for the control-plane database
     */
    public DatabaseHealthIndicator(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                    .withDetail("database", "available")
                    .build();
        } catch (final DataAccessException exception) {
            return Health.down()
                    .withDetail("database", "unavailable")
                    .withDetail("error", exception.getMessage())
                    .build();
        }
    }
}

package io.github.avinashio.ozhuku.server.health;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.health.actuate.endpoint.StatusAggregator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Actuator health groups for Ozhuku.
 *
 * <p>Spring Boot Actuator supports health groups that control which
 * {@link HealthIndicator} beans contribute to liveness, readiness,
 * and custom health endpoints. This configuration establishes:
 *
 * <ul>
 *   <li>{@code liveness} - application-only checks (no external deps)</li>
 *   <li>{@code readiness} - includes database and application checks</li>
 * </ul></p>
 *
 * <p>These beans are available for programmatic aggregation of health
 * indicators. Spring Boot also configures the health endpoint groups
 * via {@code management.endpoint.health.group.*} properties.</p>
 */
@Configuration
public class HealthGroupConfiguration {

    /**
     * Liveness health group: only the Ozhuku health indicator.
     * Liveness must NOT depend on external systems.
     */
    @Bean
    public SimpleHealthIndicatorGroup liveness(
            final OzhukuHealthIndicator ozhukuHealthIndicator) {

        return new SimpleHealthIndicatorGroup(
                "liveness",
                Map.of("ozhuku", ozhukuHealthIndicator),
                StatusAggregator.getDefault(),
                true);
    }

    /**
     * Readiness health group: includes database and application checks.
     */
    @Bean
    public SimpleHealthIndicatorGroup readiness(
            final OzhukuHealthIndicator ozhukuHealthIndicator,
            final DatabaseHealthIndicator databaseHealthIndicator) {

        final Map<String, HealthIndicator> indicators = new LinkedHashMap<>();
        indicators.put("ozhuku", ozhukuHealthIndicator);
        indicators.put("database", databaseHealthIndicator);

        return new SimpleHealthIndicatorGroup(
                "readiness",
                indicators,
                StatusAggregator.getDefault(),
                true);
    }
}

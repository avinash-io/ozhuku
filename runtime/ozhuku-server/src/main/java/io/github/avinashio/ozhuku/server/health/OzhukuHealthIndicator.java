package io.github.avinashio.ozhuku.server.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Application-level health indicator for Ozhuku.
 *
 * <p>Liveness must NOT depend on external systems. This indicator
 * checks that the application context has started successfully and
 * the core components are wired. It does not check databases, file
 * systems, or external services.</p>
 */
@Component
public class OzhukuHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("component", "ozhuku-server")
                .withDetail("status", "running")
                .build();
    }
}

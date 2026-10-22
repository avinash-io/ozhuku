package io.github.avinashio.ozhuku.server.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OzhukuHealthIndicatorTest {

    @Test
    void shouldReportHealthyApplication() {
        final OzhukuHealthIndicator indicator =
                new OzhukuHealthIndicator();

        final Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertNotNull(health.getDetails().get("component"));
        assertEquals("ozhuku-server",
                health.getDetails().get("component"));
    }
}

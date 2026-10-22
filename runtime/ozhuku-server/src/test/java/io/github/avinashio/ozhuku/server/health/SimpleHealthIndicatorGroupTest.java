package io.github.avinashio.ozhuku.server.health;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.actuate.endpoint.StatusAggregator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleHealthIndicatorGroupTest {

    @Test
    void shouldReturnUpWhenAllIndicatorsAreHealthy() {
        final HealthIndicator healthyIndicator = mock(HealthIndicator.class);
        when(healthyIndicator.health()).thenReturn(Health.up().build());

        final SimpleHealthIndicatorGroup group =
                new SimpleHealthIndicatorGroup(
                        "test",
                        Map.of("healthy", healthyIndicator),
                        StatusAggregator.getDefault(),
                        true);

        assertEquals(Status.UP, group.health().getStatus());
    }

    @Test
    void shouldReturnDownWhenAnyIndicatorIsUnhealthy() {
        final HealthIndicator healthyIndicator = mock(HealthIndicator.class);
        final HealthIndicator unhealthyIndicator = mock(HealthIndicator.class);
        when(healthyIndicator.health()).thenReturn(Health.up().build());
        when(unhealthyIndicator.health()).thenReturn(
                Health.down().withDetail("error", "fail").build());

        final SimpleHealthIndicatorGroup group =
                new SimpleHealthIndicatorGroup(
                        "test",
                        Map.of("healthy", healthyIndicator,
                                "unhealthy", unhealthyIndicator),
                        StatusAggregator.getDefault(),
                        true);

        assertEquals(Status.DOWN, group.health().getStatus());
    }

    @Test
    void shouldIncludeDetailsWhenShowDetailsTrue() {
        final HealthIndicator indicator = mock(HealthIndicator.class);
        when(indicator.health()).thenReturn(
                Health.up().withDetail("k", "v").build());

        final SimpleHealthIndicatorGroup group =
                new SimpleHealthIndicatorGroup(
                        "test",
                        Map.of("indicator", indicator),
                        StatusAggregator.getDefault(),
                        true);

        final Health health = group.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals(true,
                health.getDetails().containsKey("indicator"));
    }

    @Test
    void shouldHideDetailsWhenShowDetailsFalse() {
        final HealthIndicator indicator = mock(HealthIndicator.class);
        when(indicator.health()).thenReturn(Health.up().build());

        final SimpleHealthIndicatorGroup group =
                new SimpleHealthIndicatorGroup(
                        "test",
                        Map.of("indicator", indicator),
                        StatusAggregator.getDefault(),
                        false);

        final Health health = group.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals(false,
                health.getDetails().containsKey("indicator"));
    }

    @Test
    void shouldReportDownWhenIndicatorThrows() {
        final HealthIndicator failingIndicator = mock(HealthIndicator.class);
        when(failingIndicator.health())
                .thenThrow(new RuntimeException("boom"));

        final SimpleHealthIndicatorGroup group =
                new SimpleHealthIndicatorGroup(
                        "test",
                        Map.of("failing", failingIndicator),
                        StatusAggregator.getDefault(),
                        true);

        assertEquals(Status.DOWN, group.health().getStatus());
    }
}

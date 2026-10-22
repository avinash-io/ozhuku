package io.github.avinashio.ozhuku.server.health;

import java.util.Map;
import org.springframework.boot.health.actuate.endpoint.StatusAggregator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;

/**
 * Simple implementation of a health indicator group.
 *
 * <p>A health indicator group aggregates multiple
 * {@link HealthIndicator} instances and reports a combined status.
 * If any indicator reports a non-up status, the group is considered
 * down (unless configured otherwise).</p>
 */
public final class SimpleHealthIndicatorGroup {

    private final String name;

    private final Map<String, HealthIndicator> indicators;

    private final StatusAggregator statusAggregator;

    private final boolean showDetails;

    /**
     * Creates a simple health indicator group.
     *
     * @param name             the group name
     * @param indicators       the health indicators in this group
     * @param statusAggregator the status aggregator
     * @param showDetails      whether to show details in the response
     */
    public SimpleHealthIndicatorGroup(
            final String name,
            final Map<String, HealthIndicator> indicators,
            final StatusAggregator statusAggregator,
            final boolean showDetails) {

        this.name = name;
        this.indicators = Map.copyOf(indicators);
        this.statusAggregator = statusAggregator;
        this.showDetails = showDetails;
    }

    /**
     * Returns the group name.
     *
     * @return the group name
     */
    public String getName() {
        return name;
    }

    /**
     * Aggregates the health of all indicators in this group.
     *
     * @return the aggregated health
     */
    public Health health() {
        final Health.Builder builder = new Health.Builder();

        Status worstStatus = Status.UP;
        for (final Map.Entry<String, HealthIndicator> entry :
                indicators.entrySet()) {

            final Health health;
            try {
                health = entry.getValue().health();
            } catch (final Exception exception) {
                builder.down(exception);
                worstStatus = statusAggregator.getAggregateStatus(
                        worstStatus, Status.DOWN);
                continue;
            }

            worstStatus = statusAggregator.getAggregateStatus(
                    worstStatus, health.getStatus());

            builder.up();
        }

        if (showDetails) {
            for (final Map.Entry<String, HealthIndicator> entry :
                    indicators.entrySet()) {

                try {
                    final Health health = entry.getValue().health();
                    builder.withDetail(entry.getKey(), health);
                } catch (final Exception exception) {
                    builder.withDetail(entry.getKey(),
                            "Error: " + exception.getMessage());
                }
            }
        }

        return builder
                .status(worstStatus)
                .build();
    }
}

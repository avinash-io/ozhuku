package io.github.avinashio.ozhuku.domain.delivery;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Defines destination delivery behavior for an Ozhuku flow.
 *
 * <p>The policy describes what should happen during delivery. It does not
 * perform delivery or contain destination-specific implementation details.</p>
 */
public final class DeliveryPolicy {

    private final ConflictBehavior conflictBehavior;

    /**
     * Creates a delivery policy.
     *
     * @param conflictBehavior behavior to apply when the destination exists
     */
    public DeliveryPolicy(final ConflictBehavior conflictBehavior) {
        this.conflictBehavior = Validation.requireNonNull(
                conflictBehavior,
                "Conflict behavior must not be null");
    }

    /**
     * Returns the configured conflict behavior.
     *
     * @return conflict behavior
     */
    public ConflictBehavior conflictBehavior() {
        return conflictBehavior;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DeliveryPolicy)) {
            return false;
        }

        final DeliveryPolicy that = (DeliveryPolicy) other;
        return conflictBehavior == that.conflictBehavior;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conflictBehavior);
    }

    @Override
    public String toString() {
        return "DeliveryPolicy{"
                + "conflictBehavior=" + conflictBehavior
                + '}';
    }
}
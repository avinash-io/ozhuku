package io.github.avinashio.ozhuku.domain.retry;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.util.Objects;

/**
 * Defines retry limits and delay bounds for an Ozhuku operation.
 *
 * <p>The policy describes retry behavior but does not perform retries,
 * calculate runtime delays, or interact with infrastructure.</p>
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final long initialDelayMillis;
    private final long maxDelayMillis;

    /**
     * Creates a retry policy.
     *
     * @param maxAttempts maximum total number of attempts
     * @param initialDelayMillis initial retry delay in milliseconds
     * @param maxDelayMillis maximum retry delay in milliseconds
     */
    public RetryPolicy(
            final int maxAttempts,
            final long initialDelayMillis,
            final long maxDelayMillis) {

        if (maxAttempts < 1) {
            throw new ValidationException(
                    "Maximum attempts must be greater than zero");
        }

        if (initialDelayMillis < 0) {
            throw new ValidationException(
                    "Initial delay must not be negative");
        }

        if (maxDelayMillis < initialDelayMillis) {
            throw new ValidationException(
                    "Maximum delay must be greater than or equal to initial delay");
        }

        this.maxAttempts = maxAttempts;
        this.initialDelayMillis = initialDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
    }

    /**
     * Returns the maximum total number of attempts.
     *
     * @return maximum attempts
     */
    public int maxAttempts() {
        return maxAttempts;
    }

    /**
     * Returns the initial retry delay.
     *
     * @return initial delay in milliseconds
     */
    public long initialDelayMillis() {
        return initialDelayMillis;
    }

    /**
     * Returns the maximum retry delay.
     *
     * @return maximum delay in milliseconds
     */
    public long maxDelayMillis() {
        return maxDelayMillis;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RetryPolicy)) {
            return false;
        }

        final RetryPolicy that = (RetryPolicy) other;

        return maxAttempts == that.maxAttempts
                && initialDelayMillis == that.initialDelayMillis
                && maxDelayMillis == that.maxDelayMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                maxAttempts,
                initialDelayMillis,
                maxDelayMillis);
    }

    @Override
    public String toString() {
        return "RetryPolicy{"
                + "maxAttempts=" + maxAttempts
                + ", initialDelayMillis=" + initialDelayMillis
                + ", maxDelayMillis=" + maxDelayMillis
                + '}';
    }
}
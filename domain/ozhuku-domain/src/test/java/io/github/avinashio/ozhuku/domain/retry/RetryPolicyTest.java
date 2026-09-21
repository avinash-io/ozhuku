package io.github.avinashio.ozhuku.domain.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class RetryPolicyTest {

    @Test
    void shouldCreateValidPolicy() {
        final RetryPolicy policy =
                new RetryPolicy(5, 1_000, 30_000);

        assertEquals(5, policy.maxAttempts());
        assertEquals(1_000, policy.initialDelayMillis());
        assertEquals(30_000, policy.maxDelayMillis());
    }

    @Test
    void shouldRejectZeroAttempts() {
        assertThrows(
                ValidationException.class,
                () -> new RetryPolicy(0, 1_000, 30_000));
    }

    @Test
    void shouldRejectNegativeAttempts() {
        assertThrows(
                ValidationException.class,
                () -> new RetryPolicy(-1, 1_000, 30_000));
    }

    @Test
    void shouldRejectNegativeInitialDelay() {
        assertThrows(
                ValidationException.class,
                () -> new RetryPolicy(3, -1, 30_000));
    }

    @Test
    void shouldRejectMaximumDelayBelowInitialDelay() {
        assertThrows(
                ValidationException.class,
                () -> new RetryPolicy(3, 10_000, 5_000));
    }

    @Test
    void shouldAllowZeroDelay() {
        final RetryPolicy policy =
                new RetryPolicy(1, 0, 0);

        assertEquals(1, policy.maxAttempts());
        assertEquals(0, policy.initialDelayMillis());
        assertEquals(0, policy.maxDelayMillis());
    }

    @Test
    void shouldComparePoliciesByValue() {
        final RetryPolicy first =
                new RetryPolicy(5, 1_000, 30_000);

        final RetryPolicy second =
                new RetryPolicy(5, 1_000, 30_000);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
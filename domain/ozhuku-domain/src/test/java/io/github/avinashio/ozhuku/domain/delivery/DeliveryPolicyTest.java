package io.github.avinashio.ozhuku.domain.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class DeliveryPolicyTest {

    @Test
    void shouldCreatePolicyWithConflictBehavior() {
        final DeliveryPolicy policy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        assertEquals(
                ConflictBehavior.REPLACE,
                policy.conflictBehavior());
    }

    @Test
    void shouldRejectNullConflictBehavior() {
        assertThrows(
                ValidationException.class,
                () -> new DeliveryPolicy(null));
    }

    @Test
    void shouldComparePoliciesByValue() {
        final DeliveryPolicy first =
                new DeliveryPolicy(ConflictBehavior.SKIP);

        final DeliveryPolicy second =
                new DeliveryPolicy(ConflictBehavior.SKIP);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldRepresentPolicyAsText() {
        final DeliveryPolicy policy =
                new DeliveryPolicy(ConflictBehavior.VERSION);

        assertEquals(
                "DeliveryPolicy{conflictBehavior=VERSION}",
                policy.toString());
    }
}
package io.github.avinashio.ozhuku.domain.lease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class LeaseIdTest {

    @Test
    void shouldCreateLeaseId() {
        final LeaseId id = new LeaseId("lease-001");

        assertEquals("lease-001", id.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new LeaseId(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new LeaseId("  "));
    }

    @Test
    void shouldCompareByValue() {
        final LeaseId first = new LeaseId("lease-same");
        final LeaseId second = new LeaseId("lease-same");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentId() {
        final LeaseId first = new LeaseId("lease-1");
        final LeaseId second = new LeaseId("lease-2");

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotEqualNull() {
        final LeaseId id = new LeaseId("lease-001");

        assertNotEquals(id, null);
    }

    @Test
    void shouldNotEqualOtherType() {
        final LeaseId id = new LeaseId("lease-001");

        assertNotEquals(id, "lease-001");
    }
}

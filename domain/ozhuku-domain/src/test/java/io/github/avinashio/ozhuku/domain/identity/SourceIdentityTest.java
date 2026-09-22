package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class SourceIdentityTest {

    @Test
    void shouldCreateSourceIdentity() {
        final SourceIdentity identity =
                new SourceIdentity("source-001");

        assertEquals("source-001", identity.value());
        assertEquals("source-001", identity.toString());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new SourceIdentity(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new SourceIdentity("   "));
    }

    @Test
    void shouldCompareIdentitiesByValue() {
        final SourceIdentity first =
                new SourceIdentity("source-001");

        final SourceIdentity second =
                new SourceIdentity("source-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotTreatDifferentValuesAsEqual() {
        final SourceIdentity first =
                new SourceIdentity("source-001");

        final SourceIdentity second =
                new SourceIdentity("source-002");

        assertEquals(false, first.equals(second));
    }
}
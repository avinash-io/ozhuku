package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class SourceFingerprintTest {

    @Test
    void shouldCreateSourceFingerprint() {
        final SourceFingerprint fingerprint =
                new SourceFingerprint("fingerprint-001");

        assertEquals(
                "fingerprint-001",
                fingerprint.value());

        assertEquals(
                "fingerprint-001",
                fingerprint.toString());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new SourceFingerprint(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new SourceFingerprint("   "));
    }

    @Test
    void shouldCompareFingerprintsByValue() {
        final SourceFingerprint first =
                new SourceFingerprint("fingerprint-001");

        final SourceFingerprint second =
                new SourceFingerprint("fingerprint-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldTreatDifferentValuesAsDifferentFingerprints() {
        final SourceFingerprint first =
                new SourceFingerprint("fingerprint-001");

        final SourceFingerprint second =
                new SourceFingerprint("fingerprint-002");

        assertEquals(false, first.equals(second));
    }
}
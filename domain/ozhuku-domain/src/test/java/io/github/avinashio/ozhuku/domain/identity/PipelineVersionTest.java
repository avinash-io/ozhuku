package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PipelineVersionTest {

    @Test
    void shouldCreatePipelineVersion() {
        final PipelineVersion version = new PipelineVersion(1);

        assertEquals(1, version.value());
    }

    @Test
    void shouldRejectZero() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineVersion(0));
    }

    @Test
    void shouldRejectNegativeValue() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineVersion(-1));
    }

    @Test
    void shouldCompareByValue() {
        final PipelineVersion first = new PipelineVersion(2);
        final PipelineVersion second = new PipelineVersion(2);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentVersion() {
        final PipelineVersion first = new PipelineVersion(1);
        final PipelineVersion second = new PipelineVersion(2);

        assertNotEquals(first, second);
    }

    @Test
    void shouldReturnValueFromToString() {
        final PipelineVersion version = new PipelineVersion(3);

        assertEquals("3", version.toString());
    }
}
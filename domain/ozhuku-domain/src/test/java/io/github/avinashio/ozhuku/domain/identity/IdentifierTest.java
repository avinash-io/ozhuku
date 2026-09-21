package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class IdentifierTest {

    @Test
    void shouldCreateIdentifier() {
        final Identifier identifier = new Identifier("pipeline-001");

        assertEquals("pipeline-001", identifier.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new Identifier(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new Identifier("   "));
    }

    @Test
    void shouldCompareByValue() {
        final Identifier first = new Identifier("pipeline-001");
        final Identifier second = new Identifier("pipeline-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentValue() {
        final Identifier first = new Identifier("pipeline-001");
        final Identifier second = new Identifier("pipeline-002");

        assertNotEquals(first, second);
    }

    @Test
    void shouldReturnValueFromToString() {
        final Identifier identifier = new Identifier("pipeline-001");

        assertEquals("pipeline-001", identifier.toString());
    }
}
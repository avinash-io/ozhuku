package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ResourceIdTest {

    @Test
    void shouldCreateResourceId() {
        final ResourceId resourceId = new ResourceId("resource-001");

        assertEquals("resource-001", resourceId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new ResourceId(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new ResourceId("   "));
    }

    @Test
    void shouldCompareByValue() {
        final ResourceId first = new ResourceId("resource-001");
        final ResourceId second = new ResourceId("resource-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentResource() {
        final ResourceId first = new ResourceId("resource-001");
        final ResourceId second = new ResourceId("resource-002");

        assertNotEquals(first, second);
    }

    @Test
    void shouldReturnValueFromToString() {
        final ResourceId resourceId = new ResourceId("resource-001");

        assertEquals("resource-001", resourceId.toString());
    }
}
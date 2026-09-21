package io.github.avinashio.ozhuku.domain.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ResourceTest {

    @Test
    void shouldCreateResource() {
        final ResourceId resourceId =
                new ResourceId("resource-001");

        final ResourceLocation location =
                new ResourceLocation(
                        "file:///data/input/customer.csv");

        final Resource resource = new Resource(
                resourceId,
                location);

        assertEquals(resourceId, resource.id());
        assertEquals(location, resource.location());
    }

    @Test
    void shouldRejectNullId() {
        final ResourceLocation location =
                new ResourceLocation(
                        "file:///data/input/customer.csv");

        assertThrows(
                ValidationException.class,
                () -> new Resource(null, location));
    }

    @Test
    void shouldRejectNullLocation() {
        final ResourceId resourceId =
                new ResourceId("resource-001");

        assertThrows(
                ValidationException.class,
                () -> new Resource(resourceId, null));
    }

    @Test
    void shouldCompareResourcesByIdentity() {
        final Resource first = new Resource(
                new ResourceId("resource-001"),
                new ResourceLocation(
                        "file:///data/input/customer.csv"));

        final Resource second = new Resource(
                new ResourceId("resource-001"),
                new ResourceLocation(
                        "s3://bucket/customer.csv"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentResource() {
        final Resource first = new Resource(
                new ResourceId("resource-001"),
                new ResourceLocation(
                        "file:///data/input/customer.csv"));

        final Resource second = new Resource(
                new ResourceId("resource-002"),
                new ResourceLocation(
                        "file:///data/input/customer.csv"));

        assertNotEquals(first, second);
    }
}
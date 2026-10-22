package io.github.avinashio.ozhuku.acquisition;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScannedResourceTest {

    @Test
    void shouldCreateResourceWithFullMetadata() {

        final ScannedResource resource = ScannedResource.of(
                new ResourceId("resource-1"),
                new ResourceLocation("s3://bucket/key"),
                1024L,
                1234567890L,
                "etag-123",
                "v1");

        assertEquals("resource-1", resource.resourceId().value());
        assertEquals("s3://bucket/key", resource.location().value());
        assertEquals(1024L, resource.size());
        assertEquals(1234567890L, resource.lastModified());
        assertEquals("etag-123", resource.etag());
        assertEquals("v1", resource.version());
    }

    @Test
    void shouldCreateResourceWithMinimalMetadata() {

        final ScannedResource resource = ScannedResource.of(
                new ResourceId("resource-1"),
                new ResourceLocation("file:///data/input.csv"));

        assertEquals("resource-1", resource.resourceId().value());
        assertEquals("file:///data/input.csv", resource.location().value());
        assertEquals(-1L, resource.size());
        assertEquals(0L, resource.lastModified());
        assertNull(resource.etag());
        assertNull(resource.version());
    }

    @Test
    void shouldConvertToDomainResource() {

        final ScannedResource scanned = ScannedResource.of(
                new ResourceId("resource-1"),
                new ResourceLocation("s3://bucket/key"));

        final var resource = scanned.toResource();

        assertNotNull(resource);
        assertEquals("resource-1", resource.id().value());
        assertEquals("s3://bucket/key", resource.location().value());
    }

    @Test
    void shouldRejectNullResourceId() {
        assertThrows(
                NullPointerException.class,
                () -> ScannedResource.of(
                        null,
                        new ResourceLocation("file:///test.txt")));
    }

    @Test
    void shouldRejectNullLocation() {
        assertThrows(
                NullPointerException.class,
                () -> ScannedResource.of(
                        new ResourceId("resource-1"),
                        null));
    }
}

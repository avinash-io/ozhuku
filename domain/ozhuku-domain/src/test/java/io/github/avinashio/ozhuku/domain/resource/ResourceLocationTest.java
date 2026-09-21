package io.github.avinashio.ozhuku.domain.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ResourceLocationTest {

    @Test
    void shouldCreateFileLocation() {
        final ResourceLocation location =
                new ResourceLocation("file:///data/input/customer.csv");

        assertEquals(
                "file:///data/input/customer.csv",
                location.value());
        assertEquals("file", location.scheme());
    }

    @Test
    void shouldCreateS3Location() {
        final ResourceLocation location =
                new ResourceLocation("s3://customer-bucket/incoming/customer.csv");

        assertEquals("s3", location.scheme());
        assertEquals(
                "s3://customer-bucket/incoming/customer.csv",
                location.value());
    }

    @Test
    void shouldRejectNullLocation() {
        assertThrows(
                ValidationException.class,
                () -> new ResourceLocation(null));
    }

    @Test
    void shouldRejectBlankLocation() {
        assertThrows(
                ValidationException.class,
                () -> new ResourceLocation("   "));
    }

    @Test
    void shouldRejectLocationWithoutScheme() {
        assertThrows(
                ValidationException.class,
                () -> new ResourceLocation("/data/input/customer.csv"));
    }

    @Test
    void shouldRejectMalformedUri() {
        assertThrows(
                ValidationException.class,
                () -> new ResourceLocation("://invalid"));
    }

    @Test
    void shouldCompareByLocation() {
        final ResourceLocation first =
                new ResourceLocation("s3://bucket/file.csv");

        final ResourceLocation second =
                new ResourceLocation("s3://bucket/file.csv");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
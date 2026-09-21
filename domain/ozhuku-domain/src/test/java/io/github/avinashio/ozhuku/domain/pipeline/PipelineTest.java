package io.github.avinashio.ozhuku.domain.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PipelineTest {

    @Test
    void shouldCreatePipeline() {
        final PipelineId id = new PipelineId("customer-import");

        final Pipeline pipeline = new Pipeline(
                id,
                "Customer Import");

        assertEquals(id, pipeline.id());
        assertEquals("Customer Import", pipeline.name());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(
                ValidationException.class,
                () -> new Pipeline(null, "Customer Import"));
    }

    @Test
    void shouldRejectBlankName() {
        final PipelineId id = new PipelineId("customer-import");

        assertThrows(
                ValidationException.class,
                () -> new Pipeline(id, "   "));
    }

    @Test
    void shouldComparePipelinesByIdentity() {
        final Pipeline first = new Pipeline(
                new PipelineId("customer-import"),
                "Customer Import");

        final Pipeline second = new Pipeline(
                new PipelineId("customer-import"),
                "Customer Import V2");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentPipeline() {
        final Pipeline first = new Pipeline(
                new PipelineId("customer-import"),
                "Customer Import");

        final Pipeline second = new Pipeline(
                new PipelineId("order-import"),
                "Order Import");

        assertNotEquals(first, second);
    }
}
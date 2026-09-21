package io.github.avinashio.ozhuku.domain.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PipelineDefinitionTest {

    @Test
    void shouldCreateDefinition() {
        final PipelineId pipelineId = new PipelineId("customer-import");
        final PipelineVersion version = new PipelineVersion(1);

        final PipelineDefinition definition = new PipelineDefinition(
                pipelineId,
                version,
                "Customer import");

        assertEquals(pipelineId, definition.pipelineId());
        assertEquals(version, definition.version());
        assertEquals("Customer import", definition.description());
    }

    @Test
    void shouldRejectNullPipelineId() {
        final PipelineVersion version = new PipelineVersion(1);

        assertThrows(
                ValidationException.class,
                () -> new PipelineDefinition(
                        null,
                        version,
                        "Customer import"));
    }

    @Test
    void shouldRejectNullVersion() {
        final PipelineId pipelineId = new PipelineId("customer-import");

        assertThrows(
                ValidationException.class,
                () -> new PipelineDefinition(
                        pipelineId,
                        null,
                        "Customer import"));
    }

    @Test
    void shouldNormalizeDescriptionWhitespace() {
        final PipelineDefinition definition = new PipelineDefinition(
                new PipelineId("customer-import"),
                new PipelineVersion(1),
                "  Customer import  ");

        assertEquals("Customer import", definition.description());
    }

    @Test
    void shouldUseEmptyDescriptionWhenNotSupplied() {
        final PipelineDefinition definition = new PipelineDefinition(
                new PipelineId("customer-import"),
                new PipelineVersion(1),
                null);

        assertEquals("", definition.description());
    }

    @Test
    void shouldCompareByPipelineAndVersion() {
        final PipelineDefinition first = new PipelineDefinition(
                new PipelineId("customer-import"),
                new PipelineVersion(1),
                "First description");

        final PipelineDefinition second = new PipelineDefinition(
                new PipelineId("customer-import"),
                new PipelineVersion(1),
                "Different description");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentVersion() {
        final PipelineDefinition first = new PipelineDefinition(
                new PipelineId("customer-import"),
                new PipelineVersion(1),
                "Customer import");

        final PipelineDefinition second = new PipelineDefinition(
                new PipelineId("customer-import"),
                new PipelineVersion(2),
                "Customer import");

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotEqualDifferentPipeline() {
        final PipelineDefinition first = new PipelineDefinition(
                new PipelineId("customer-import"),
                new PipelineVersion(1),
                "Customer import");

        final PipelineDefinition second = new PipelineDefinition(
                new PipelineId("order-import"),
                new PipelineVersion(1),
                "Order import");

        assertNotEquals(first, second);
    }
}
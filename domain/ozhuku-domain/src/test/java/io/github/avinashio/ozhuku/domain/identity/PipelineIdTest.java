package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PipelineIdTest {

    @Test
    void shouldCreatePipelineId() {
        final PipelineId pipelineId = new PipelineId("pipeline-001");

        assertEquals("pipeline-001", pipelineId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineId(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new PipelineId("   "));
    }

    @Test
    void shouldCompareByValue() {
        final PipelineId first = new PipelineId("pipeline-001");
        final PipelineId second = new PipelineId("pipeline-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualExecutionId() {
        final PipelineId pipelineId = new PipelineId("execution-001");
        final ExecutionId executionId = new ExecutionId("execution-001");

        assertNotEquals(pipelineId, executionId);
    }

    @Test
    void shouldReturnValueFromToString() {
        final PipelineId pipelineId = new PipelineId("pipeline-001");

        assertEquals("pipeline-001", pipelineId.toString());
    }
}
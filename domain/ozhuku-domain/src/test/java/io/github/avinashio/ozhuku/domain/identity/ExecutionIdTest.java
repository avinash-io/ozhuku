package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ExecutionIdTest {

    @Test
    void shouldCreateExecutionId() {
        final ExecutionId executionId = new ExecutionId("execution-001");

        assertEquals("execution-001", executionId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new ExecutionId(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new ExecutionId("   "));
    }

    @Test
    void shouldCompareByValue() {
        final ExecutionId first = new ExecutionId("execution-001");
        final ExecutionId second = new ExecutionId("execution-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualPipelineId() {
        final ExecutionId executionId = new ExecutionId("pipeline-001");
        final PipelineId pipelineId = new PipelineId("pipeline-001");

        assertNotEquals(executionId, pipelineId);
    }

    @Test
    void shouldReturnValueFromToString() {
        final ExecutionId executionId = new ExecutionId("execution-001");

        assertEquals("execution-001", executionId.toString());
    }
}
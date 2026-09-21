package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ExecutionReferenceTest {

    @Test
    void shouldCreateReference() {
        final ExecutionId executionId =
                new ExecutionId("execution-001");
        final PipelineId pipelineId =
                new PipelineId("customer-import");
        final PipelineVersion version =
                new PipelineVersion(3);

        final ExecutionReference reference =
                new ExecutionReference(
                        executionId,
                        pipelineId,
                        version);

        assertEquals(executionId, reference.executionId());
        assertEquals(pipelineId, reference.pipelineId());
        assertEquals(version, reference.pipelineVersion());
    }

    @Test
    void shouldRejectNullExecutionId() {
        assertThrows(
                ValidationException.class,
                () -> new ExecutionReference(
                        null,
                        new PipelineId("pipeline"),
                        new PipelineVersion(1)));
    }

    @Test
    void shouldRejectNullPipelineId() {
        assertThrows(
                ValidationException.class,
                () -> new ExecutionReference(
                        new ExecutionId("execution"),
                        null,
                        new PipelineVersion(1)));
    }

    @Test
    void shouldRejectNullPipelineVersion() {
        assertThrows(
                ValidationException.class,
                () -> new ExecutionReference(
                        new ExecutionId("execution"),
                        new PipelineId("pipeline"),
                        null));
    }

    @Test
    void shouldCompareReferencesByValue() {
        final ExecutionReference first =
                new ExecutionReference(
                        new ExecutionId("execution"),
                        new PipelineId("pipeline"),
                        new PipelineVersion(2));

        final ExecutionReference second =
                new ExecutionReference(
                        new ExecutionId("execution"),
                        new PipelineId("pipeline"),
                        new PipelineVersion(2));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldRepresentReferenceAsText() {
        final ExecutionReference reference =
                new ExecutionReference(
                        new ExecutionId("execution"),
                        new PipelineId("pipeline"),
                        new PipelineVersion(2));

        assertEquals(
                "ExecutionReference{"
                        + "executionId=execution"
                        + ", pipelineId=pipeline"
                        + ", pipelineVersion=2"
                        + '}',
                reference.toString());
    }
}
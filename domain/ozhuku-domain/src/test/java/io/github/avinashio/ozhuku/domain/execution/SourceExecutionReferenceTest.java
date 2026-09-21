package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class SourceExecutionReferenceTest {

    @Test
    void shouldCreateReference() {
        final ExecutionId executionId =
                new ExecutionId("execution-001");
        final ResourceId resourceId =
                new ResourceId("resource-001");

        final SourceExecutionReference reference =
                new SourceExecutionReference(
                        executionId,
                        resourceId);

        assertEquals(executionId, reference.executionId());
        assertEquals(resourceId, reference.resourceId());
    }

    @Test
    void shouldRejectNullExecutionId() {
        assertThrows(
                ValidationException.class,
                () -> new SourceExecutionReference(
                        null,
                        new ResourceId("resource-001")));
    }

    @Test
    void shouldRejectNullResourceId() {
        assertThrows(
                ValidationException.class,
                () -> new SourceExecutionReference(
                        new ExecutionId("execution-001"),
                        null));
    }

    @Test
    void shouldCompareReferencesByValue() {
        final SourceExecutionReference first =
                new SourceExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        final SourceExecutionReference second =
                new SourceExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotTreatDifferentResourcesAsEqual() {
        final SourceExecutionReference first =
                new SourceExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        final SourceExecutionReference second =
                new SourceExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-002"));

        assertEquals(false, first.equals(second));
    }

    @Test
    void shouldRepresentReferenceAsText() {
        final SourceExecutionReference reference =
                new SourceExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        assertEquals(
                "SourceExecutionReference{"
                        + "executionId=execution-001"
                        + ", resourceId=resource-001"
                        + '}',
                reference.toString());
    }
}
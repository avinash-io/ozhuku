package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class DestinationExecutionReferenceTest {

    @Test
    void shouldCreateReference() {
        final ExecutionId executionId =
                new ExecutionId("execution-001");
        final ResourceId resourceId =
                new ResourceId("resource-001");

        final DestinationExecutionReference reference =
                new DestinationExecutionReference(
                        executionId,
                        resourceId);

        assertEquals(executionId, reference.executionId());
        assertEquals(resourceId, reference.resourceId());
    }

    @Test
    void shouldRejectNullExecutionId() {
        assertThrows(
                ValidationException.class,
                () -> new DestinationExecutionReference(
                        null,
                        new ResourceId("resource-001")));
    }

    @Test
    void shouldRejectNullResourceId() {
        assertThrows(
                ValidationException.class,
                () -> new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        null));
    }

    @Test
    void shouldCompareReferencesByValue() {
        final DestinationExecutionReference first =
                new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        final DestinationExecutionReference second =
                new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotTreatDifferentResourcesAsEqual() {
        final DestinationExecutionReference first =
                new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        final DestinationExecutionReference second =
                new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-002"));

        assertEquals(false, first.equals(second));
    }

    @Test
    void shouldRepresentReferenceAsText() {
        final DestinationExecutionReference reference =
                new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        assertEquals(
                "DestinationExecutionReference{"
                        + "executionId=execution-001"
                        + ", resourceId=resource-001"
                        + '}',
                reference.toString());
    }
}
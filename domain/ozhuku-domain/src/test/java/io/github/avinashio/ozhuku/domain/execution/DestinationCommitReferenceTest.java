package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class DestinationCommitReferenceTest {

    @Test
    void shouldCreateReference() {
        final DestinationExecutionReference executionReference =
                new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("destination-001"));

        final DestinationCommitReference reference =
                new DestinationCommitReference(executionReference);

        assertEquals(
                executionReference,
                reference.destinationExecutionReference());
    }

    @Test
    void shouldRejectNullDestinationExecutionReference() {
        assertThrows(
                ValidationException.class,
                () -> new DestinationCommitReference(null));
    }

    @Test
    void shouldCompareReferencesByValue() {
        final DestinationCommitReference first =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                new ExecutionId("execution-001"),
                                new ResourceId("destination-001")));

        final DestinationCommitReference second =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                new ExecutionId("execution-001"),
                                new ResourceId("destination-001")));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotTreatDifferentDestinationsAsEqual() {
        final DestinationCommitReference first =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                new ExecutionId("execution-001"),
                                new ResourceId("destination-001")));

        final DestinationCommitReference second =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                new ExecutionId("execution-001"),
                                new ResourceId("destination-002")));

        assertEquals(false, first.equals(second));
    }

    @Test
    void shouldRepresentReferenceAsText() {
        final DestinationCommitReference reference =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                new ExecutionId("execution-001"),
                                new ResourceId("destination-001")));

        assertEquals(
                "DestinationCommitReference{"
                        + "destinationExecutionReference="
                        + "DestinationExecutionReference{"
                        + "executionId=execution-001"
                        + ", resourceId=destination-001"
                        + "}"
                        + '}',
                reference.toString());
    }
}
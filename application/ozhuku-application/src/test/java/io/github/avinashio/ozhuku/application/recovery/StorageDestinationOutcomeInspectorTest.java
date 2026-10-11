
package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.storage.DestinationWriteEvidenceStore;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;

class StorageDestinationOutcomeInspectorTest {

    private static final DestinationCommitReference REFERENCE =
            new DestinationCommitReference(
                    new DestinationExecutionReference(
                            new ExecutionId("execution-inspector-test"),
                            new ResourceId("resource-inspector-test")));

    @Test
    void shouldReturnCommittedStatus() {
        final StorageDestinationOutcomeInspector inspector =
                inspectorReturning(CommitStatus.COMMITTED);

        assertEquals(
                CommitStatus.COMMITTED,
                inspector.inspect(REFERENCE));
    }

    @Test
    void shouldReturnNotCommittedStatus() {
        final StorageDestinationOutcomeInspector inspector =
                inspectorReturning(CommitStatus.NOT_COMMITTED);

        assertEquals(
                CommitStatus.NOT_COMMITTED,
                inspector.inspect(REFERENCE));
    }

    @Test
    void shouldPreserveUnknownStatus() {
        final StorageDestinationOutcomeInspector inspector =
                inspectorReturning(CommitStatus.UNKNOWN);

        assertEquals(
                CommitStatus.UNKNOWN,
                inspector.inspect(REFERENCE));
    }

    @Test
    void shouldWrapEvidenceStoreIOException() {
        final IOException expectedException =
                new IOException("Evidence store unavailable");

        final DestinationWriteEvidenceStore evidenceStore =
                new DestinationWriteEvidenceStore() {
                    @Override
                    public void recordCommitted(
                            final DestinationCommitReference reference) {
                        // Avinash: This test exercises inspection only.
                    }

                    @Override
                    public CommitStatus inspect(
                            final DestinationCommitReference reference)
                            throws IOException {
                        throw expectedException;
                    }
                };

        final StorageDestinationOutcomeInspector inspector =
                new StorageDestinationOutcomeInspector(evidenceStore);

        final UncheckedIOException actualException =
                assertThrows(
                        UncheckedIOException.class,
                        () -> inspector.inspect(REFERENCE));

        assertEquals(
                expectedException,
                actualException.getCause());
    }

    @Test
    void shouldRejectNullReference() {
        final StorageDestinationOutcomeInspector inspector =
                inspectorReturning(CommitStatus.UNKNOWN);

        assertThrows(
                NullPointerException.class,
                () -> inspector.inspect(null));
    }

    @Test
    void shouldRejectNullEvidenceStore() {
        assertThrows(
                NullPointerException.class,
                () -> new StorageDestinationOutcomeInspector(null));
    }

    @Test
    void shouldRejectNullInspectionResult() {
        final StorageDestinationOutcomeInspector inspector =
                inspectorReturning(null);

        assertThrows(
                NullPointerException.class,
                () -> inspector.inspect(REFERENCE));
    }

    private static StorageDestinationOutcomeInspector inspectorReturning(
            final CommitStatus status) {

        final DestinationWriteEvidenceStore evidenceStore =
                new DestinationWriteEvidenceStore() {
                    @Override
                    public void recordCommitted(
                            final DestinationCommitReference reference) {
                        // Avinash: This test exercises inspection only.
                    }

                    @Override
                    public CommitStatus inspect(
                            final DestinationCommitReference reference) {
                        return status;
                    }
                };

        return new StorageDestinationOutcomeInspector(evidenceStore);
    }
}


package io.github.avinashio.ozhuku.storage.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileDestinationWriteEvidenceStoreTest {

    @TempDir
    Path temporaryDirectory;

    private FileDestinationWriteEvidenceStore store;

    @BeforeEach
    void setUp() {
        store = new FileDestinationWriteEvidenceStore(
                temporaryDirectory);
    }

    @Test
    void inspectReturnsUnknownWhenEvidenceIsMissing()
            throws IOException {

        assertEquals(
                CommitStatus.UNKNOWN,
                store.inspect(reference("execution-1", "resource-1")));
    }

    @Test
    void recordCommittedAllowsEvidenceToBeInspected()
            throws IOException {

        final DestinationCommitReference reference =
                reference("execution-1", "resource-1");

        store.recordCommitted(reference);

        assertEquals(
                CommitStatus.COMMITTED,
                store.inspect(reference));
    }

    @Test
    void evidenceIsScopedToExecutionAndResource()
            throws IOException {

        store.recordCommitted(
                reference("execution-1", "resource-1"));

        assertEquals(
                CommitStatus.UNKNOWN,
                store.inspect(
                        reference("execution-2", "resource-1")));

        assertEquals(
                CommitStatus.UNKNOWN,
                store.inspect(
                        reference("execution-1", "resource-2")));
    }

    @Test
    void inspectReturnsUnknownForInvalidEvidence()
            throws IOException {

        final DestinationCommitReference reference =
                reference("execution-1", "resource-1");

        store.recordCommitted(reference);

        final Path marker;

        try (java.util.stream.Stream<Path> files =
                     java.nio.file.Files.walk(
                             temporaryDirectory.resolve(".ozhuku"))) {
            marker = files
                    .filter(path -> path.getFileName().toString()
                            .endsWith(".marker"))
                    .findFirst()
                    .orElseThrow();
        }

        java.nio.file.Files.writeString(marker, "invalid-evidence");

        assertEquals(
                CommitStatus.UNKNOWN,
                store.inspect(reference));
    }

    @Test
    void recordCommittedRejectsNullReference() {
        assertThrows(
                NullPointerException.class,
                () -> store.recordCommitted(null));
    }

    private DestinationCommitReference reference(
            final String executionId,
            final String resourceId) {

        return new DestinationCommitReference(
                new DestinationExecutionReference(
                        new ExecutionId(executionId),
                        new ResourceId(resourceId)));
    }


    @Test
    void inspectReturnsUnknownWhenEvidenceDirectoryIsMissing()
            throws IOException {

        final DestinationCommitReference reference =
                reference("execution-missing-directory", "resource-1");

        assertEquals(
                CommitStatus.UNKNOWN,
                store.inspect(reference));
    }

    @Test
    void recordCommittedCanBeRepeatedForSameReference()
            throws IOException {

        final DestinationCommitReference reference =
                reference("execution-repeat", "resource-repeat");

        store.recordCommitted(reference);
        store.recordCommitted(reference);

        assertEquals(
                CommitStatus.COMMITTED,
                store.inspect(reference));
    }

}

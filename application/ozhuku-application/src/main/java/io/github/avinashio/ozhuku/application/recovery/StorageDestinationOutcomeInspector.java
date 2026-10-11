
package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.storage.DestinationWriteEvidenceStore;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public final class StorageDestinationOutcomeInspector
        implements DestinationOutcomeInspector {

    private final DestinationWriteEvidenceStore evidenceStore;

    public StorageDestinationOutcomeInspector(
            final DestinationWriteEvidenceStore evidenceStore) {
        this.evidenceStore = Objects.requireNonNull(
                evidenceStore,
                "evidenceStore must not be null");
    }

    @Override
    public CommitStatus inspect(
            final DestinationCommitReference reference) {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        try {
            return Objects.requireNonNull(
                    evidenceStore.inspect(reference),
                    "inspected destination status must not be null");
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Could not inspect destination write evidence",
                    exception);
        }
    }
}

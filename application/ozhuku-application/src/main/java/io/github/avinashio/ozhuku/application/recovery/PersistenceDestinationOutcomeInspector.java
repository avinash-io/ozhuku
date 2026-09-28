package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;

public final class PersistenceDestinationOutcomeInspector
        implements DestinationOutcomeInspector {

    private final DestinationCommitRepository commitRepository;

    public PersistenceDestinationOutcomeInspector(
            final DestinationCommitRepository commitRepository) {
        this.commitRepository =
                Validation.requireNonNull(
                        commitRepository,
                        "commitRepository");
    }

    @Override
    public CommitStatus inspect(
            final DestinationCommitReference reference) {

        Validation.requireNonNull(
                reference,
                "reference");

        return commitRepository
                .findById(reference)
                .map(DestinationCommit::status)
                .orElse(CommitStatus.UNKNOWN);
    }
}
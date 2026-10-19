package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Reconciles the persisted commit status of destination executions with
 * independent storage evidence.
 *
 * <p>This service discovers destination commits in the {@code UNKNOWN} state
 * and inspects the storage evidence to determine the true outcome:
 * <ul>
 *   <li>{@code COMMITTED} - durable destination acceptance confirmed</li>
 *   <li>{@code NOT_COMMITTED} - durable destination acceptance not confirmed</li>
 *   <li>{@code UNKNOWN} - outcome cannot be established from storage evidence</li>
 * </ul>
 *
 * <p>Missing or invalid evidence does not prove the destination was not
 * committed; it results in {@code UNKNOWN}, preserving the safety guarantee
 * that {@code UNKNOWN} is never blindly retried.
 *
 * <p>The reconciliation is idempotent - repeated invocations produce the same
 * persisted state because storage evidence inspection is deterministic.
 */
public final class DestinationOutcomeReconciliationService {

    private final DestinationCommitRepository commitRepository;
    private final DestinationOutcomeInspector outcomeInspector;

    public DestinationOutcomeReconciliationService(
            final DestinationCommitRepository commitRepository,
            final DestinationOutcomeInspector outcomeInspector) {

        this.commitRepository = Validation.requireNonNull(
                commitRepository,
                "commitRepository must not be null");

        this.outcomeInspector = Validation.requireNonNull(
                outcomeInspector,
                "outcomeInspector must not be null");
    }

    /**
     * Reconciles all destination commits currently in the {@code UNKNOWN} state.
     *
     * <p>For each UNKNOWN commit, inspects the storage evidence and updates
     * the persisted commit status accordingly:
     * <ul>
     *   <li>Evidence confirms commit -> {@code COMMITTED}</li>
     *   <li>Evidence confirms not committed -> {@code NOT_COMMITTED}</li>
     *   <li>Evidence missing or invalid -> remains {@code UNKNOWN}</li>
     * </ul>
     *
     * <p>If inspection fails due to an {@link IOException}, the exception is
     * wrapped and thrown - the commit remains in {@code UNKNOWN} state and
     * will be retried on the next reconciliation cycle.
     *
     * @throws IllegalStateException if reconciliation fails for any commit
     */
    public void reconcileUnknownCommits() throws IOException {
        final List<DestinationCommit> unknownCommits =
                commitRepository.findByStatus(CommitStatus.UNKNOWN);

        for (DestinationCommit commit : unknownCommits) {
            reconcileSingleCommit(commit);
        }
    }

    /**
     * Reconciles a single destination commit with storage evidence.
     *
     * <p>This method is idempotent and safe to call multiple times.
     *
     * @param commit the commit to reconcile (must be in UNKNOWN state)
     * @throws IllegalArgumentException if commit is not in UNKNOWN state
     * @throws IllegalStateException if inspection or persistence fails
     */
    public void reconcileSingleCommit(final DestinationCommit commit)
            throws IOException {

        Objects.requireNonNull(commit, "commit must not be null");

        if (commit.status() != CommitStatus.UNKNOWN) {
            throw new IllegalArgumentException(
                    "Can only reconcile commits in UNKNOWN state, found: "
                            + commit.status());
        }

        final DestinationCommitReference reference = commit.reference();

        // Inspect storage evidence
        final CommitStatus inspectedStatus = outcomeInspector.inspect(reference);

        // Update persisted commit based on inspection result
        final DestinationCommit reconciledCommit = switch (inspectedStatus) {
            case COMMITTED ->
                    DestinationCommit.committed(reference, java.time.Instant.now());

            case NOT_COMMITTED ->
                    DestinationCommit.notCommitted(reference);

            case UNKNOWN ->
                    // Remains UNKNOWN - no evidence found or evidence invalid
                    // This preserves the safety guarantee: UNKNOWN is not blindly retried
                    commit;
        };

        // Persist the reconciled status
        if (reconciledCommit != commit) {
            commitRepository.save(reconciledCommit);
        }
    }

    /**
     * Returns the number of commits currently in UNKNOWN state without
     * performing reconciliation.
     *
     * @return count of UNKNOWN commits
     */
    public int countUnknownCommits() {
        return commitRepository.findByStatus(CommitStatus.UNKNOWN).size();
    }
}
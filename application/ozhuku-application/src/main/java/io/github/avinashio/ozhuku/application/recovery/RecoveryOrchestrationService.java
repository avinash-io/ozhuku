package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.application.recovery.DestinationExecutionRecoveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Production recovery orchestration that discovers and processes
 * recoverable destination executions using existing recovery services.
 *
 * This is the smallest safe mechanism that leverages the existing recovery
 * decision logic (DestinationRecoveryDecider) which already handles:
 * - COMMITTED -> do not retry
 * - NOT_COMMITTED -> retry
 * - UNKNOWN -> inspect outcome, retreat only if NOT_COMMITTED
 */
public final class RecoveryOrchestrationService {

    private final DestinationExecutionRepository destinationExecutionRepository;
    private final ExecutionRepository executionRepository;
    private final DestinationRecoveryExecutor recoveryExecutor;
    private final DestinationRecoveryService recoveryService;

    public RecoveryOrchestrationService(
            final DestinationExecutionRepository destinationExecutionRepository,
            final ExecutionRepository executionRepository,
            final DestinationRecoveryExecutor recoveryExecutor,
            final DestinationRecoveryService recoveryService) {

        this.destinationExecutionRepository =
                Objects.requireNonNull(
                        destinationExecutionRepository,
                        "destinationExecutionRepository must not be null");

        this.executionRepository =
                Objects.requireNonNull(
                        executionRepository,
                        "executionRepository must not be null");

        this.recoveryExecutor =
                Objects.requireNonNull(
                        recoveryExecutor,
                        "recoveryExecutor must not be null");

        this.recoveryService =
                Objects.requireNonNull(
                        recoveryService,
                        "recoveryService must not be null");
    }

    /**
     * Discover and process all recoverable destination executions.
     * <p>
     * This method:
     * <li>Queries for executions with PENDING, RUNNING, or FAILED status</li>
     * <li>Verifies each execution exists and is recoverable</li>
     * <li>Processes each candidate through the existing recovery executor</li>
     * <li>Handles individual candidate failures without unsafe retries</li>
     * <li>Is idempotent - repeated invocations produce the same outcome</li>
     * </ul>
     * </p>
     *
     * @throws IllegalStateException if discovery or processing fails
     */
    public void processRecoverableCandidates() {

        // Step 1: Discover recoverable candidates (PENDING, RUNNING, FAILED)
        List<DestinationExecution> candidates =
                destinationExecutionRepository.findRecoverableExecutions();

        // Step 2: Process each candidate
        for (DestinationExecution candidate : candidates) {

            // Verify the execution still exists and is in a recoverable state
            Optional<Execution> executionOptional =
                    executionRepository.findById(candidate.reference().executionId());

            if (executionOptional.isEmpty()) {
                continue; // Execution no longer exists, skip
            }

            Execution execution = executionOptional.get();

            // Skip if execution is in a non-recoverable state (COMPLETED, CANCELLED)
            if (execution.status() == ExecutionStatus.COMPLETED
                    || execution.status() == ExecutionStatus.CANCELLED) {
                continue;
            }

            // Create a recovery reference from the candidate
            DestinationExecutionReference recoveryRef =
                    new DestinationExecutionReference(
                            candidate.reference().executionId(),
                            candidate.reference().resourceId());

            // Step 3: Process through existing recovery executor
            // The existing DestinationRecoveryExecutor.execute() already handles:
            // - COMMITTED -> do not retry (RecoveryDecision.DO_NOT_RETRY)
            // - NOT_COMMITTED -> retry (RecoveryDecision.RETRY)
            // - UNKNOWN -> inspect outcome, retreat if NOT_COMMITTED
            // No blind retries of UNKNOWN outcomes
            try {
                recoveryExecutor.execute(recoveryRef);
            } catch (IOException e) {
                // Individual candidate failure - do not convert to unsafe retry
                // The outcome inspection logic in DestinationRecoveryDecider ensures
                // UNKNOWN outcomes are never blindly retried
                throw new IllegalStateException(
                        "Recovery failed for candidate: "
                                + recoveryRef,
                        e);
            }
        }
    }

    /**
     * Discover recoverable candidates without processing them.
     * <p>
     * Returns a list of DestinationExecution references that are eligible
     * for recovery (PENDING, RUNNING, or FAILED status).
     * </p>
     *
     * @return unmodifiable list of recoverable candidates
     */
    public List<DestinationExecution> discoverRecoverableCandidates() {
        return List.copyOf(
                destinationExecutionRepository.findRecoverableExecutions());
    }
}

package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import org.junit.jupiter.api.Test;

class DestinationExecutionRecoveryPolicyTest {

    private final DestinationExecutionRecoveryPolicy policy =
            new DestinationExecutionRecoveryPolicy();

    @Test
    void shouldAllowRecoveryForPendingExecution() {
        assertTrue(
                policy.canRecover(
                        DestinationExecutionStatus.PENDING));
    }

    @Test
    void shouldAllowRecoveryForRunningExecution() {
        assertTrue(
                policy.canRecover(
                        DestinationExecutionStatus.RUNNING));
    }

    @Test
    void shouldAllowRecoveryForFailedExecution() {
        assertTrue(
                policy.canRecover(
                        DestinationExecutionStatus.FAILED));
    }

    @Test
    void shouldNotAllowRecoveryForCompletedExecution() {
        assertFalse(
                policy.canRecover(
                        DestinationExecutionStatus.COMPLETED));
    }

    @Test
    void shouldNotAllowRecoveryForCancelledExecution() {
        assertFalse(
                policy.canRecover(
                        DestinationExecutionStatus.CANCELLED));
    }
}
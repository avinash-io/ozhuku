package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;

public final class DestinationExecutionRecoveryPolicy {

    public boolean canRecover(
            final DestinationExecutionStatus status) {

        return switch (status) {
            case PENDING, RUNNING, FAILED ->
                    true;

            case COMPLETED, CANCELLED ->
                    false;
        };
    }
}
package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;

public interface RecoveryDecisionProvider {

    RecoveryResult decide(
            DestinationExecutionReference reference);
}


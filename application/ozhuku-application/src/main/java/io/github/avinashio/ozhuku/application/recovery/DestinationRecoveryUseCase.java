package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import java.util.Objects;

public final class DestinationRecoveryUseCase {

    private final RecoveryDecisionProvider recoveryDecisionProvider;

    public DestinationRecoveryUseCase(
            final RecoveryDecisionProvider recoveryDecisionProvider) {

        this.recoveryDecisionProvider =
                Objects.requireNonNull(
                        recoveryDecisionProvider,
                        "recoveryDecisionProvider must not be null");
    }

    public RecoveryResult execute(
            final DestinationExecutionReference reference) {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        return recoveryDecisionProvider.decide(reference);
    }
}

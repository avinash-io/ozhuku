package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

public final class RecoveryResult {

    private final RecoveryDecision decision;
    private final RecoveryReason reason;

    public RecoveryResult(
            final RecoveryDecision decision,
            final RecoveryReason reason) {

        this.decision = Validation.requireNonNull(
                decision,
                "Recovery decision must not be null");

        this.reason = Validation.requireNonNull(
                reason,
                "Recovery reason must not be null");
    }

    public RecoveryDecision decision() {
        return decision;
    }

    public RecoveryReason reason() {
        return reason;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RecoveryResult)) {
            return false;
        }

        final RecoveryResult that =
                (RecoveryResult) other;

        return decision == that.decision
                && reason == that.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(decision, reason);
    }

    @Override
    public String toString() {
        return "RecoveryResult{"
                + "decision=" + decision
                + ", reason=" + reason
                + '}';
    }
}
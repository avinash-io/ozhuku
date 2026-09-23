package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class RecoveryResultTest {

    @Test
    void shouldStoreDecisionAndReason() {
        final RecoveryResult result =
                new RecoveryResult(
                        RecoveryDecision.RETRY,
                        RecoveryReason.COMMIT_NOT_CONFIRMED);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_NOT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldSupportEquality() {
        final RecoveryResult first =
                new RecoveryResult(
                        RecoveryDecision.RETRY,
                        RecoveryReason.COMMIT_NOT_CONFIRMED);

        final RecoveryResult second =
                new RecoveryResult(
                        RecoveryDecision.RETRY,
                        RecoveryReason.COMMIT_NOT_CONFIRMED);

        assertEquals(first, second);
        assertEquals(
                first.hashCode(),
                second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDecisionDiffers() {
        final RecoveryResult retry =
                new RecoveryResult(
                        RecoveryDecision.RETRY,
                        RecoveryReason.COMMIT_NOT_CONFIRMED);

        final RecoveryResult noRetry =
                new RecoveryResult(
                        RecoveryDecision.DO_NOT_RETRY,
                        RecoveryReason.COMMIT_CONFIRMED);

        assertNotEquals(retry, noRetry);
    }

    @Test
    void shouldNotBeEqualWhenReasonDiffers() {
        final RecoveryResult first =
                new RecoveryResult(
                        RecoveryDecision.RETRY,
                        RecoveryReason.COMMIT_NOT_CONFIRMED);

        final RecoveryResult second =
                new RecoveryResult(
                        RecoveryDecision.RETRY,
                        RecoveryReason.DESTINATION_OUTCOME_NOT_CONFIRMED);

        assertNotEquals(first, second);
    }
}
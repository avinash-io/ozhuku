package io.github.avinashio.ozhuku.domain.deduplication;

import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.foundation.validation.Validation;

/**
 * Evaluates duplicate processing policy against existing processing history.
 */
public final class DeduplicationEvaluator {

    /**
     * Determines whether processing should proceed.
     *
     * @param policy duplicate handling policy
     * @param existingRecord previously persisted processing record, or null
     * @param currentFingerprint current source fingerprint
     * @return deduplication decision
     */
    public DeduplicationDecision evaluate(
            final DuplicatePolicy policy,
            final ProcessingRecord existingRecord,
            final SourceFingerprint currentFingerprint) {

        Validation.requireNonNull(
                policy,
                "Duplicate policy must not be null");

        Validation.requireNonNull(
                currentFingerprint,
                "Current source fingerprint must not be null");

        if (existingRecord == null
                || existingRecord.status()
                != ProcessingStatus.PROCESSED) {
            return DeduplicationDecision.PROCESS;
        }

        return switch (policy) {
            case SKIP_IF_PROCESSED ->
                    DeduplicationDecision.SKIP;

            case REPROCESS_IF_CHANGED ->
                    evaluateChangedSource(
                            existingRecord,
                            currentFingerprint);

            case ALWAYS_PROCESS ->
                    DeduplicationDecision.PROCESS;

            case FAIL_IF_DUPLICATE ->
                    DeduplicationDecision.FAIL;
        };
    }

    private DeduplicationDecision evaluateChangedSource(
            final ProcessingRecord existingRecord,
            final SourceFingerprint currentFingerprint) {

        if (existingRecord.sourceFingerprint()
                .equals(currentFingerprint)) {
            return DeduplicationDecision.SKIP;
        }

        return DeduplicationDecision.PROCESS;
    }
}
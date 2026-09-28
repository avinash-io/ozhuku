package io.github.avinashio.ozhuku.application.deduplication;

import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationDecision;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationEvaluator;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import java.util.Optional;

/**
 * Coordinates durable processing history with duplicate policy evaluation.
 *
 * <p>This service owns the application-layer interaction between the
 * processing history repository and the domain deduplication evaluator.
 * It does not perform resource processing, delivery, or business
 * transformation.</p>
 */
public final class DeduplicationService {

    private final ProcessingRecordRepository processingRecordRepository;
    private final DeduplicationEvaluator deduplicationEvaluator;

    /**
     * Creates a deduplication service.
     *
     * @param processingRecordRepository durable processing history repository
     * @param deduplicationEvaluator domain duplicate policy evaluator
     */
    public DeduplicationService(
            final ProcessingRecordRepository processingRecordRepository,
            final DeduplicationEvaluator deduplicationEvaluator) {

        this.processingRecordRepository = Validation.requireNonNull(
                processingRecordRepository,
                "Processing record repository must not be null");

        this.deduplicationEvaluator = Validation.requireNonNull(
                deduplicationEvaluator,
                "Deduplication evaluator must not be null");
    }

    /**
     * Evaluates whether processing should proceed for a processing identity.
     *
     * @param identity processing identity
     * @param policy duplicate handling policy
     * @param currentFingerprint current source fingerprint
     * @return deduplication decision
     */
    public DeduplicationDecision evaluate(
            final ProcessingIdentity identity,
            final DuplicatePolicy policy,
            final SourceFingerprint currentFingerprint) {

        Validation.requireNonNull(
                identity,
                "Processing identity must not be null");

        Validation.requireNonNull(
                policy,
                "Duplicate policy must not be null");

        Validation.requireNonNull(
                currentFingerprint,
                "Current source fingerprint must not be null");

        final Optional<ProcessingRecord> existingRecord =
                processingRecordRepository.findByIdentity(identity);

        return deduplicationEvaluator.evaluate(
                policy,
                existingRecord.orElse(null),
                currentFingerprint);
    }
}
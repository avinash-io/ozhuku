package io.github.avinashio.ozhuku.domain.deduplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DeduplicationEvaluatorTest {

    private static final SourceFingerprint FINGERPRINT =
            new SourceFingerprint("fingerprint-001");

    private static final SourceFingerprint CHANGED_FINGERPRINT =
            new SourceFingerprint("fingerprint-002");

    @Test
    void shouldProcessWhenNoPreviousRecordExists() {
        final DeduplicationDecision decision =
                evaluator().evaluate(
                        DuplicatePolicy.SKIP_IF_PROCESSED,
                        null,
                        FINGERPRINT);

        assertEquals(
                DeduplicationDecision.PROCESS,
                decision);
    }

    @Test
    void shouldProcessWhenPreviousRecordWasNotProcessed() {
        final DeduplicationDecision decision =
                evaluator().evaluate(
                        DuplicatePolicy.SKIP_IF_PROCESSED,
                        ProcessingRecord.notProcessed(identity()),
                        FINGERPRINT);

        assertEquals(
                DeduplicationDecision.PROCESS,
                decision);
    }

    @Test
    void shouldSkipAlreadyProcessedSourceByDefault() {
        final DeduplicationDecision decision =
                evaluator().evaluate(
                        DuplicatePolicy.SKIP_IF_PROCESSED,
                        processedRecord(FINGERPRINT),
                        FINGERPRINT);

        assertEquals(
                DeduplicationDecision.SKIP,
                decision);
    }

    @Test
    void shouldSkipUnchangedSourceWithReprocessIfChanged() {
        final DeduplicationDecision decision =
                evaluator().evaluate(
                        DuplicatePolicy.REPROCESS_IF_CHANGED,
                        processedRecord(FINGERPRINT),
                        FINGERPRINT);

        assertEquals(
                DeduplicationDecision.SKIP,
                decision);
    }

    @Test
    void shouldProcessChangedSourceWithReprocessIfChanged() {
        final DeduplicationDecision decision =
                evaluator().evaluate(
                        DuplicatePolicy.REPROCESS_IF_CHANGED,
                        processedRecord(FINGERPRINT),
                        CHANGED_FINGERPRINT);

        assertEquals(
                DeduplicationDecision.PROCESS,
                decision);
    }

    @Test
    void shouldAlwaysProcessWhenPolicyRequiresIt() {
        final DeduplicationDecision decision =
                evaluator().evaluate(
                        DuplicatePolicy.ALWAYS_PROCESS,
                        processedRecord(FINGERPRINT),
                        FINGERPRINT);

        assertEquals(
                DeduplicationDecision.PROCESS,
                decision);
    }

    @Test
    void shouldFailWhenDuplicatePolicyRequiresFailure() {
        final DeduplicationDecision decision =
                evaluator().evaluate(
                        DuplicatePolicy.FAIL_IF_DUPLICATE,
                        processedRecord(FINGERPRINT),
                        FINGERPRINT);

        assertEquals(
                DeduplicationDecision.FAIL,
                decision);
    }

    @Test
    void shouldRejectNullPolicy() {
        assertThrows(
                ValidationException.class,
                () -> evaluator().evaluate(
                        null,
                        null,
                        FINGERPRINT));
    }

    @Test
    void shouldRejectNullCurrentFingerprint() {
        assertThrows(
                ValidationException.class,
                () -> evaluator().evaluate(
                        DuplicatePolicy.SKIP_IF_PROCESSED,
                        null,
                        null));
    }

    private DeduplicationEvaluator evaluator() {
        return new DeduplicationEvaluator();
    }

    private ProcessingRecord processedRecord(
            final SourceFingerprint fingerprint) {

        return ProcessingRecord.processed(
                identity(),
                fingerprint,
                Instant.parse(
                        "2026-09-22T10:00:00Z"));
    }

    private ProcessingIdentity identity() {
        return new ProcessingIdentity(
                new SourceIdentity("source-001"),
                new PipelineId("pipeline-001"),
                new PipelineVersion(1));
    }
}
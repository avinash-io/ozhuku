package io.github.avinashio.ozhuku.application.deduplication;

import io.github.avinashio.ozhuku.application.source.SourceFingerprintService;
import io.github.avinashio.ozhuku.application.source.SourceIdentityService;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationDecision;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;
import java.util.Objects;

/**
 * Coordinates execution-specific source identity and deduplication evaluation.
 *
 * Avinash: Keep duplicate-policy evaluation in the domain/application
 * deduplication services rather than embedding it in execution processing.
 */
public final class ExecutionDeduplicationService {

    private final SourceIdentityService sourceIdentityService;
    private final SourceFingerprintService sourceFingerprintService;
    private final DeduplicationService deduplicationService;

    public ExecutionDeduplicationService(
            final SourceIdentityService sourceIdentityService,
            final SourceFingerprintService sourceFingerprintService,
            final DeduplicationService deduplicationService) {

        this.sourceIdentityService = Objects.requireNonNull(
                sourceIdentityService,
                "sourceIdentityService must not be null");

        this.sourceFingerprintService = Objects.requireNonNull(
                sourceFingerprintService,
                "sourceFingerprintService must not be null");

        this.deduplicationService = Objects.requireNonNull(
                deduplicationService,
                "deduplicationService must not be null");
    }

    public Result evaluate(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan,
            final Resource source)
            throws IOException {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                pipelinePlan,
                "pipelinePlan must not be null");

        Objects.requireNonNull(
                source,
                "source must not be null");

        final SourceIdentity sourceIdentity =
                sourceIdentityService.identify(source);

        final SourceFingerprint sourceFingerprint =
                sourceFingerprintService.fingerprint(source);

        final ProcessingIdentity processingIdentity =
                new ProcessingIdentity(
                        sourceIdentity,
                        executionReference.pipelineId(),
                        executionReference.pipelineVersion());

        final DuplicatePolicy duplicatePolicy =
                pipelinePlan.duplicatePolicy();

        final DeduplicationDecision decision =
                deduplicationService.evaluate(
                        processingIdentity,
                        duplicatePolicy,
                        sourceFingerprint);

        return new Result(
                processingIdentity,
                sourceFingerprint,
                decision);
    }

    public record Result(
            ProcessingIdentity processingIdentity,
            SourceFingerprint sourceFingerprint,
            DeduplicationDecision decision) {

        public Result {
            Objects.requireNonNull(
                    processingIdentity,
                    "processingIdentity must not be null");

            Objects.requireNonNull(
                    sourceFingerprint,
                    "sourceFingerprint must not be null");

            Objects.requireNonNull(
                    decision,
                    "decision must not be null");
        }
    }
}

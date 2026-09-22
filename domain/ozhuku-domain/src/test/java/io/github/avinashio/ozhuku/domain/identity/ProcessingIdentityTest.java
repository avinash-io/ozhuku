package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ProcessingIdentityTest {

    @Test
    void shouldCreateProcessingIdentity() {
        final SourceIdentity sourceIdentity =
                new SourceIdentity("source-001");
        final PipelineId pipelineId =
                new PipelineId("pipeline-001");
        final PipelineVersion pipelineVersion =
                new PipelineVersion(3);

        final ProcessingIdentity identity =
                new ProcessingIdentity(
                        sourceIdentity,
                        pipelineId,
                        pipelineVersion);

        assertEquals(sourceIdentity, identity.sourceIdentity());
        assertEquals(pipelineId, identity.pipelineId());
        assertEquals(pipelineVersion, identity.pipelineVersion());
    }

    @Test
    void shouldRejectNullSourceIdentity() {
        assertThrows(
                ValidationException.class,
                () -> new ProcessingIdentity(
                        null,
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(1)));
    }

    @Test
    void shouldRejectNullPipelineId() {
        assertThrows(
                ValidationException.class,
                () -> new ProcessingIdentity(
                        new SourceIdentity("source-001"),
                        null,
                        new PipelineVersion(1)));
    }

    @Test
    void shouldRejectNullPipelineVersion() {
        assertThrows(
                ValidationException.class,
                () -> new ProcessingIdentity(
                        new SourceIdentity("source-001"),
                        new PipelineId("pipeline-001"),
                        null));
    }

    @Test
    void shouldCompareIdentitiesByValue() {
        final ProcessingIdentity first = identity();
        final ProcessingIdentity second = identity();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldTreatDifferentPipelineIdsAsDifferentIdentities() {
        final ProcessingIdentity first =
                new ProcessingIdentity(
                        new SourceIdentity("source-001"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(1));

        final ProcessingIdentity second =
                new ProcessingIdentity(
                        new SourceIdentity("source-001"),
                        new PipelineId("pipeline-002"),
                        new PipelineVersion(1));

        assertEquals(false, first.equals(second));
    }

    @Test
    void shouldTreatDifferentPipelineVersionsAsDifferentIdentities() {
        final ProcessingIdentity first =
                new ProcessingIdentity(
                        new SourceIdentity("source-001"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(1));

        final ProcessingIdentity second =
                new ProcessingIdentity(
                        new SourceIdentity("source-001"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(2));

        assertEquals(false, first.equals(second));
    }

    @Test
    void shouldTreatDifferentSourceIdentitiesAsDifferentIdentities() {
        final ProcessingIdentity first =
                new ProcessingIdentity(
                        new SourceIdentity("source-001"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(1));

        final ProcessingIdentity second =
                new ProcessingIdentity(
                        new SourceIdentity("source-002"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(1));

        assertEquals(false, first.equals(second));
    }

    @Test
    void shouldRepresentIdentityAsText() {
        final ProcessingIdentity identity = identity();

        assertEquals(
                "ProcessingIdentity{"
                        + "sourceIdentity=source-001"
                        + ", pipelineId=pipeline-001"
                        + ", pipelineVersion=2"
                        + '}',
                identity.toString());
    }

    private ProcessingIdentity identity() {
        return new ProcessingIdentity(
                new SourceIdentity("source-001"),
                new PipelineId("pipeline-001"),
                new PipelineVersion(2));
    }
}
package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DestinationCommitTest {

    @Test
    void shouldCreateNotCommittedState() {
        final DestinationCommitReference reference = reference();

        final DestinationCommit commit =
                DestinationCommit.notCommitted(reference);

        assertEquals(reference, commit.reference());
        assertEquals(CommitStatus.NOT_COMMITTED, commit.status());
        assertNull(commit.committedAt());
    }

    @Test
    void shouldCreateUnknownState() {
        final DestinationCommitReference reference = reference();

        final DestinationCommit commit =
                DestinationCommit.unknown(reference);

        assertEquals(reference, commit.reference());
        assertEquals(CommitStatus.UNKNOWN, commit.status());
        assertNull(commit.committedAt());
    }

    @Test
    void shouldCreateCommittedState() {
        final DestinationCommitReference reference = reference();
        final Instant committedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final DestinationCommit commit =
                DestinationCommit.committed(
                        reference,
                        committedAt);

        assertEquals(reference, commit.reference());
        assertEquals(CommitStatus.COMMITTED, commit.status());
        assertEquals(committedAt, commit.committedAt());
    }

    @Test
    void shouldRejectNullReference() {
        assertThrows(
                ValidationException.class,
                () -> DestinationCommit.notCommitted(null));
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                ValidationException.class,
                () -> new DestinationCommit(reference(), null));
    }

    @Test
    void shouldRejectCommittedStatusWithoutTimestamp() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DestinationCommit(
                        reference(),
                        CommitStatus.COMMITTED));
    }

    @Test
    void shouldRejectNullCommitTimestamp() {
        assertThrows(
                ValidationException.class,
                () -> DestinationCommit.committed(
                        reference(),
                        null));
    }

    @Test
    void shouldCompareEqualCommitStatesByValue() {
        final Instant committedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final DestinationCommit first =
                DestinationCommit.committed(
                        reference(),
                        committedAt);

        final DestinationCommit second =
                DestinationCommit.committed(
                        reference(),
                        committedAt);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private DestinationCommitReference reference() {
        return new DestinationCommitReference(
                new DestinationExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("destination-001")));
    }
}
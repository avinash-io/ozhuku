package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PersistenceDestinationOutcomeInspectorTest {

    private static final DestinationCommitReference REFERENCE =
            new DestinationCommitReference(
                    new DestinationExecutionReference(
                            new ExecutionId("execution-1"),
                            new ResourceId("destination-1")));

    @Test
    void shouldReturnCommittedStatus() {

        final InMemoryDestinationCommitRepository repository =
                new InMemoryDestinationCommitRepository();

        repository.save(
                DestinationCommit.committed(
                        REFERENCE,
                        Instant.now()));

        final PersistenceDestinationOutcomeInspector inspector =
                new PersistenceDestinationOutcomeInspector(
                        repository);

        assertEquals(
                CommitStatus.COMMITTED,
                inspector.inspect(REFERENCE));
    }

    @Test
    void shouldReturnNotCommittedStatus() {

        final InMemoryDestinationCommitRepository repository =
                new InMemoryDestinationCommitRepository();

        repository.save(
                DestinationCommit.notCommitted(
                        REFERENCE));

        final PersistenceDestinationOutcomeInspector inspector =
                new PersistenceDestinationOutcomeInspector(
                        repository);

        assertEquals(
                CommitStatus.NOT_COMMITTED,
                inspector.inspect(REFERENCE));
    }

    @Test
    void shouldReturnUnknownStatus() {

        final InMemoryDestinationCommitRepository repository =
                new InMemoryDestinationCommitRepository();

        repository.save(
                DestinationCommit.unknown(
                        REFERENCE));

        final PersistenceDestinationOutcomeInspector inspector =
                new PersistenceDestinationOutcomeInspector(
                        repository);

        assertEquals(
                CommitStatus.UNKNOWN,
                inspector.inspect(REFERENCE));
    }

    @Test
    void shouldTreatMissingCommitAsUnknown() {

        final InMemoryDestinationCommitRepository repository =
                new InMemoryDestinationCommitRepository();

        final PersistenceDestinationOutcomeInspector inspector =
                new PersistenceDestinationOutcomeInspector(
                        repository);

        assertEquals(
                CommitStatus.UNKNOWN,
                inspector.inspect(REFERENCE));
    }

    @Test
    void shouldRejectNullReference() {

        final PersistenceDestinationOutcomeInspector inspector =
                new PersistenceDestinationOutcomeInspector(
                        new InMemoryDestinationCommitRepository());

        assertThrows(
                ValidationException.class,
                () -> inspector.inspect(null));
    }

    @Test
    void shouldRejectNullRepository() {

        assertThrows(
                ValidationException.class,
                () -> new PersistenceDestinationOutcomeInspector(
                        null));
    }

    private static final class InMemoryDestinationCommitRepository
            implements DestinationCommitRepository {

        private final Map<
                DestinationCommitReference,
                DestinationCommit> commits =
                new HashMap<>();

        @Override
        public Optional<DestinationCommit> findById(
                final DestinationCommitReference reference) {

            return Optional.ofNullable(
                    commits.get(reference));
        }

        @Override
        public void save(
                final DestinationCommit destinationCommit) {

            commits.put(
                    destinationCommit.reference(),
                    destinationCommit);
        }
    }
}

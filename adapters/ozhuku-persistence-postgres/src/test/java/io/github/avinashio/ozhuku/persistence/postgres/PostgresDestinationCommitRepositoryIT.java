package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresDestinationCommitRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    private static DataSource dataSource;

    private static DestinationCommitRepository repository;

    @BeforeAll
    static void setUp() {
        dataSource = createDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        repository =
                new PostgresDestinationCommitRepository(
                        dataSource);
    }

    @Test
    void shouldReturnEmptyWhenDestinationCommitDoesNotExist() {
        final DestinationCommitReference reference =
                reference(
                        "execution-unknown",
                        "resource-unknown");

        final Optional<DestinationCommit> result =
                repository.findById(reference);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrieveNotCommittedState() {
        final DestinationCommitReference reference =
                reference(
                        "execution-not-committed",
                        "resource-not-committed");

        final DestinationCommit commit =
                DestinationCommit.notCommitted(reference);

        repository.save(commit);

        final Optional<DestinationCommit> result =
                repository.findById(reference);

        assertTrue(result.isPresent());
        assertEquals(
                commit,
                result.orElseThrow());
        assertEquals(
                CommitStatus.NOT_COMMITTED,
                result.orElseThrow().status());
        assertEquals(
                null,
                result.orElseThrow().committedAt());
    }

    @Test
    void shouldSaveAndRetrieveCommittedState() {
        final DestinationCommitReference reference =
                reference(
                        "execution-committed",
                        "resource-committed");

        final Instant committedAt =
                Instant.parse("2026-09-22T12:00:00Z");

        final DestinationCommit commit =
                DestinationCommit.committed(
                        reference,
                        committedAt);

        repository.save(commit);

        final Optional<DestinationCommit> result =
                repository.findById(reference);

        assertTrue(result.isPresent());

        final DestinationCommit retrieved =
                result.orElseThrow();

        assertEquals(
                CommitStatus.COMMITTED,
                retrieved.status());
        assertEquals(
                committedAt,
                retrieved.committedAt());
        assertEquals(
                commit,
                retrieved);
    }

    @Test
    void shouldSaveAndRetrieveUnknownState() {
        final DestinationCommitReference reference =
                reference(
                        "execution-unknown-state",
                        "resource-unknown-state");

        final DestinationCommit commit =
                DestinationCommit.unknown(reference);

        repository.save(commit);

        final Optional<DestinationCommit> result =
                repository.findById(reference);

        assertTrue(result.isPresent());

        final DestinationCommit retrieved =
                result.orElseThrow();

        assertEquals(
                CommitStatus.UNKNOWN,
                retrieved.status());
        assertEquals(
                null,
                retrieved.committedAt());
        assertEquals(
                commit,
                retrieved);
    }

    @Test
    void shouldKeepDifferentDestinationIdentitiesSeparate() {
        final DestinationCommitReference firstReference =
                reference(
                        "execution-same",
                        "resource-one");

        final DestinationCommitReference secondReference =
                reference(
                        "execution-same",
                        "resource-two");

        final DestinationCommit first =
                DestinationCommit.committed(
                        firstReference,
                        Instant.parse(
                                "2026-09-22T13:00:00Z"));

        final DestinationCommit second =
                DestinationCommit.unknown(
                        secondReference);

        repository.save(first);
        repository.save(second);

        final Optional<DestinationCommit> firstResult =
                repository.findById(firstReference);

        final Optional<DestinationCommit> secondResult =
                repository.findById(secondReference);

        assertTrue(firstResult.isPresent());
        assertTrue(secondResult.isPresent());

        assertEquals(
                first,
                firstResult.orElseThrow());

        assertEquals(
                second,
                secondResult.orElseThrow());
    }

    @Test
    void shouldReplaceExistingCommitState() {
        final DestinationCommitReference reference =
                reference(
                        "execution-replace",
                        "resource-replace");

        final DestinationCommit unknown =
                DestinationCommit.unknown(reference);

        final Instant committedAt =
                Instant.parse("2026-09-22T14:00:00Z");

        final DestinationCommit committed =
                DestinationCommit.committed(
                        reference,
                        committedAt);

        repository.save(unknown);
        repository.save(committed);

        final Optional<DestinationCommit> result =
                repository.findById(reference);

        assertTrue(result.isPresent());

        final DestinationCommit retrieved =
                result.orElseThrow();

        assertEquals(
                CommitStatus.COMMITTED,
                retrieved.status());
        assertEquals(
                committedAt,
                retrieved.committedAt());
        assertEquals(
                committed,
                retrieved);
    }

    private static DestinationCommitReference reference(
            final String executionId,
            final String resourceId) {

        return new DestinationCommitReference(
                new DestinationExecutionReference(
                        new ExecutionId(executionId),
                        new ResourceId(resourceId)));
    }

    private static DataSource createDataSource() {
        final org.postgresql.ds.PGSimpleDataSource dataSource =
                new org.postgresql.ds.PGSimpleDataSource();

        dataSource.setServerNames(
                new String[]{POSTGRES.getHost()});
        dataSource.setPortNumbers(
                new int[]{POSTGRES.getMappedPort(5432)});
        dataSource.setDatabaseName(
                POSTGRES.getDatabaseName());
        dataSource.setUser(
                POSTGRES.getUsername());
        dataSource.setPassword(
                POSTGRES.getPassword());

        return dataSource;
    }
}
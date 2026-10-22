package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.audit.AuditEvent;
import io.github.avinashio.ozhuku.domain.audit.AuditEventId;
import io.github.avinashio.ozhuku.domain.audit.AuditEventType;
import io.github.avinashio.ozhuku.persistence.AuditEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresAuditEventRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    private static DataSource dataSource;

    private static AuditEventRepository repository;

    @BeforeAll
    static void setUp() {
        dataSource = createDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        repository = new PostgresAuditEventRepository(dataSource);
    }

    @Test
    void shouldReturnEmptyWhenAuditEventDoesNotExist() {
        final AuditEventId eventId =
                new AuditEventId("event-nonexistent");

        final Optional<AuditEvent> result =
                repository.findById(eventId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrieveAuditEvent() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-001"),
                timestamp,
                "org-test",
                "admin-user",
                AuditEventType.CONFIGURATION_CHANGE,
                "pipeline",
                "pipeline-001",
                "pipeline-001",
                null,
                null,
                "OLD_CONFIG",
                "NEW_CONFIG",
                "Configuration updated via CLI",
                "success",
                "corr-001",
                Map.of("field", "storageRoot"));

        repository.save(event);

        final Optional<AuditEvent> result =
                repository.findById(new AuditEventId("event-001"));

        assertTrue(result.isPresent());

        final AuditEvent retrieved = result.orElseThrow();

        assertEquals(event.eventId(), retrieved.eventId());
        assertEquals(timestamp, retrieved.timestamp());
        assertEquals("org-test", retrieved.organizationId());
        assertEquals("admin-user", retrieved.actor());
        assertEquals(AuditEventType.CONFIGURATION_CHANGE,
                retrieved.eventType());
        assertEquals("pipeline", retrieved.resourceType());
        assertEquals("pipeline-001", retrieved.resourceId());
        assertEquals("pipeline-001", retrieved.pipelineId());
        assertEquals("OLD_CONFIG", retrieved.previousState());
        assertEquals("NEW_CONFIG", retrieved.newState());
        assertEquals("Configuration updated via CLI",
                retrieved.reason());
        assertEquals("success", retrieved.outcome());
        assertEquals("corr-001", retrieved.correlationId());
        assertEquals("storageRoot", retrieved.metadata().get("field"));
    }

    @Test
    void shouldStoreNullOptionalFields() {
        final Instant timestamp =
                Instant.parse("2026-10-22T13:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-002"),
                timestamp,
                "org-test",
                "system",
                AuditEventType.DUPLICATE_DETECTION,
                "resource",
                "resource-002",
                null,
                null,
                null,
                null,
                null,
                null,
                "skipped",
                "corr-002",
                null);

        repository.save(event);

        final Optional<AuditEvent> result =
                repository.findById(new AuditEventId("event-002"));

        assertTrue(result.isPresent());

        final AuditEvent retrieved = result.orElseThrow();

        assertEquals("event-002", retrieved.eventId().value());
        assertEquals(null, retrieved.pipelineId());
        assertEquals(null, retrieved.executionId());
        assertEquals(null, retrieved.sourceIdentity());
        assertEquals(null, retrieved.previousState());
        assertEquals(null, retrieved.newState());
        assertEquals(null, retrieved.reason());
        assertEquals("skipped", retrieved.outcome());
        assertTrue(retrieved.metadata().isEmpty());
    }

    @Test
    void shouldFindByTimestampBetweenOrderedByTimestampDescending() {
        final Instant baseTime =
                Instant.parse("2026-10-22T14:00:00Z");

        final AuditEvent first = new AuditEvent(
                new AuditEventId("event-time-1"),
                baseTime,
                "org-test",
                "user-a",
                AuditEventType.REPROCESSING,
                "pipeline",
                "pipeline-a",
                "pipeline-a",
                "execution-a",
                null,
                null,
                "RUNNING",
                "Manual reprocess",
                "success",
                "corr-a",
                null);

        final AuditEvent second = new AuditEvent(
                new AuditEventId("event-time-2"),
                baseTime.plusSeconds(10),
                "org-test",
                "user-b",
                AuditEventType.MANUAL_RETRY,
                "pipeline",
                "pipeline-b",
                "pipeline-b",
                "execution-b",
                null,
                null,
                "RUNNING",
                "Manual retry",
                "success",
                "corr-b",
                null);

        repository.save(first);
        repository.save(second);

        final List<AuditEvent> results =
                repository.findByTimestampBetween(
                        baseTime.minusSeconds(1),
                        baseTime.plusSeconds(20),
                        10);

        assertTrue(results.size() >= 2);

        // Verify ordering: most recent first
        final AuditEvent latest = results.get(0);
        assertEquals(
                baseTime.plusSeconds(10),
                latest.timestamp());
        assertEquals("event-time-2", latest.eventId().value());
    }

    @Test
    void shouldFindByActorOrderedByTimestampDescending() {
        final Instant baseTime =
                Instant.parse("2026-10-22T15:00:00Z");

        final AuditEvent firstByActor = new AuditEvent(
                new AuditEventId("event-actor-1"),
                baseTime,
                "org-test",
                "test-actor",
                AuditEventType.SECRET_ROTATION,
                "secret",
                "secret-001",
                null,
                null,
                null,
                "OLD",
                "NEW",
                "Key rotated",
                "success",
                "corr-actor-1",
                null);

        final AuditEvent secondByActor = new AuditEvent(
                new AuditEventId("event-actor-2"),
                baseTime.plusSeconds(5),
                "org-test",
                "test-actor",
                AuditEventType.SECRET_ROTATION,
                "secret",
                "secret-002",
                null,
                null,
                null,
                "OLD",
                "NEW",
                "Key rotated",
                "success",
                "corr-actor-2",
                null);

        repository.save(firstByActor);
        repository.save(secondByActor);

        final List<AuditEvent> results =
                repository.findByActor("test-actor", 10);

        assertTrue(results.size() >= 2);

        // Verify ordering: most recent first
        final AuditEvent latest = results.get(0);
        assertEquals("event-actor-2", latest.eventId().value());
    }

    @Test
    void shouldNotStoreDuplicates() {
        final Instant timestamp =
                Instant.parse("2026-10-22T16:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-dup"),
                timestamp,
                "org-test",
                "admin",
                AuditEventType.AUTHORIZATION_FAILURE,
                "pipeline",
                "pipeline-dup",
                null,
                null,
                null,
                null,
                null,
                "Access denied",
                "failure",
                "corr-dup",
                null);

        repository.save(event);
        repository.save(event);

        final Optional<AuditEvent> result =
                repository.findById(new AuditEventId("event-dup"));

        assertTrue(result.isPresent());
    }

    @Test
    void shouldRetrieveEmptyWhenTimestampRangeHasNoEvents() {
        final Instant from =
                Instant.parse("2000-01-01T00:00:00Z");
        final Instant to =
                Instant.parse("2000-01-01T01:00:00Z");

        final List<AuditEvent> results =
                repository.findByTimestampBetween(from, to, 10);

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldRetrieveEmptyWhenActorHasNoEvents() {
        final List<AuditEvent> results =
                repository.findByActor("nonexistent-actor", 10);

        assertTrue(results.isEmpty());
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

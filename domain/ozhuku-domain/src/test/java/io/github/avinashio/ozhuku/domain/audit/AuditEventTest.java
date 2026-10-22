package io.github.avinashio.ozhuku.domain.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditEventTest {

    @Test
    void shouldCreateAuditEventWithAllFields() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-001"),
                timestamp,
                "org-test",
                "admin",
                AuditEventType.CONFIGURATION_CHANGE,
                "pipeline",
                "pipeline-001",
                "pipeline-001",
                "execution-001",
                "source-001",
                "OLD",
                "NEW",
                "Config changed",
                "success",
                "corr-001",
                Map.of("key", "value"));

        assertEquals("event-001", event.eventId().value());
        assertEquals(timestamp, event.timestamp());
        assertEquals("org-test", event.organizationId());
        assertEquals("admin", event.actor());
        assertEquals(AuditEventType.CONFIGURATION_CHANGE,
                event.eventType());
        assertEquals("pipeline", event.resourceType());
        assertEquals("pipeline-001", event.resourceId());
        assertEquals("pipeline-001", event.pipelineId());
        assertEquals("execution-001", event.executionId());
        assertEquals("source-001", event.sourceIdentity());
        assertEquals("OLD", event.previousState());
        assertEquals("NEW", event.newState());
        assertEquals("Config changed", event.reason());
        assertEquals("success", event.outcome());
        assertEquals("corr-001", event.correlationId());
        assertEquals("value", event.metadata().get("key"));
    }

    @Test
    void shouldAcceptNullForOptionalFields() {
        final Instant timestamp =
                Instant.parse("2026-10-22T13:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-002"),
                timestamp,
                "org-test",
                "system",
                AuditEventType.DUPLICATE_DETECTION,
                "resource",
                "resource-001",
                null,
                null,
                null,
                null,
                null,
                null,
                "skipped",
                "corr-002",
                null);

        assertEquals(null, event.pipelineId());
        assertEquals(null, event.executionId());
        assertEquals(null, event.sourceIdentity());
        assertEquals(null, event.previousState());
        assertEquals(null, event.newState());
        assertEquals(null, event.reason());
        assertTrue(event.metadata().isEmpty());
    }

    @Test
    void shouldRejectNullEventId() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        null,
                        timestamp,
                        "org",
                        "actor",
                        AuditEventType.REPROCESSING,
                        "resource",
                        "id",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "success",
                        "corr",
                        null));
    }

    @Test
    void shouldRejectNullTimestamp() {
        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        new AuditEventId("event-003"),
                        null,
                        "org",
                        "actor",
                        AuditEventType.REPROCESSING,
                        "resource",
                        "id",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "success",
                        "corr",
                        null));
    }

    @Test
    void shouldRejectBlankOrganizationId() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        new AuditEventId("event-004"),
                        timestamp,
                        "  ",
                        "actor",
                        AuditEventType.REPROCESSING,
                        "resource",
                        "id",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "success",
                        "corr",
                        null));
    }

    @Test
    void shouldRejectNullActor() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        new AuditEventId("event-005"),
                        timestamp,
                        "org",
                        null,
                        AuditEventType.REPROCESSING,
                        "resource",
                        "id",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "success",
                        "corr",
                        null));
    }

    @Test
    void shouldRejectNullEventType() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        new AuditEventId("event-006"),
                        timestamp,
                        "org",
                        "actor",
                        null,
                        "resource",
                        "id",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "success",
                        "corr",
                        null));
    }

    @Test
    void shouldRejectBlankResourceId() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        new AuditEventId("event-007"),
                        timestamp,
                        "org",
                        "actor",
                        AuditEventType.REPROCESSING,
                        "resource",
                        "  ",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "success",
                        "corr",
                        null));
    }

    @Test
    void shouldRejectNullOutcome() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        new AuditEventId("event-008"),
                        timestamp,
                        "org",
                        "actor",
                        AuditEventType.REPROCESSING,
                        "resource",
                        "id",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "corr",
                        null));
    }

    @Test
    void shouldRejectBlankCorrelationId() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        assertThrows(
                ValidationException.class,
                () -> new AuditEvent(
                        new AuditEventId("event-009"),
                        timestamp,
                        "org",
                        "actor",
                        AuditEventType.REPROCESSING,
                        "resource",
                        "id",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "success",
                        "  ",
                        null));
    }

    @Test
    void shouldReturnImmutableMetadataCopy() {
        final Instant timestamp =
                Instant.parse("2026-10-22T14:00:00Z");

        final Map<String, String> original =
                Map.of("key1", "value1");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-010"),
                timestamp,
                "org",
                "actor",
                AuditEventType.REPROCESSING,
                "resource",
                "id",
                null,
                null,
                null,
                null,
                null,
                null,
                "success",
                "corr",
                original);

        final Map<String, String> metadata = event.metadata();

        assertThrows(
                UnsupportedOperationException.class,
                () -> metadata.put("key2", "value2"));
    }

    @Test
    void shouldReturnEmptyMapForNullMetadata() {
        final Instant timestamp =
                Instant.parse("2026-10-22T15:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-011"),
                timestamp,
                "org",
                "actor",
                AuditEventType.REPROCESSING,
                "resource",
                "id",
                null,
                null,
                null,
                null,
                null,
                null,
                "success",
                "corr",
                null);

        assertTrue(event.metadata().isEmpty());
    }

    @Test
    void shouldCompareByEventId() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        final AuditEvent first = new AuditEvent(
                new AuditEventId("event-same"),
                timestamp,
                "org",
                "actor",
                AuditEventType.REPROCESSING,
                "resource",
                "id",
                null,
                null,
                null,
                null,
                null,
                null,
                "success",
                "corr",
                null);

        final AuditEvent second = new AuditEvent(
                new AuditEventId("event-same"),
                timestamp,
                "org2",
                "actor2",
                AuditEventType.RECOVERY,
                "resource2",
                "id2",
                null,
                null,
                null,
                null,
                null,
                null,
                "failure",
                "corr2",
                null);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentEventId() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        final AuditEvent first = new AuditEvent(
                new AuditEventId("event-1"),
                timestamp,
                "org",
                "actor",
                AuditEventType.REPROCESSING,
                "resource",
                "id",
                null,
                null,
                null,
                null,
                null,
                null,
                "success",
                "corr",
                null);

        final AuditEvent second = new AuditEvent(
                new AuditEventId("event-2"),
                timestamp,
                "org",
                "actor",
                AuditEventType.REPROCESSING,
                "resource",
                "id",
                null,
                null,
                null,
                null,
                null,
                null,
                "success",
                "corr",
                null);

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotEqualNull() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-012"),
                timestamp,
                "org",
                "actor",
                AuditEventType.REPROCESSING,
                "resource",
                "id",
                null,
                null,
                null,
                null,
                null,
                null,
                "success",
                "corr",
                null);

        assertNotEquals(event, null);
    }

    @Test
    void shouldNotEqualOtherType() {
        final Instant timestamp =
                Instant.parse("2026-10-22T12:00:00Z");

        final AuditEvent event = new AuditEvent(
                new AuditEventId("event-013"),
                timestamp,
                "org",
                "actor",
                AuditEventType.REPROCESSING,
                "resource",
                "id",
                null,
                null,
                null,
                null,
                null,
                null,
                "success",
                "corr",
                null);

        assertNotEquals(event, "some-string");
    }
}

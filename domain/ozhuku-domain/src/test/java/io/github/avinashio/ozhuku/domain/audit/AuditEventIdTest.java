package io.github.avinashio.ozhuku.domain.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class AuditEventIdTest {

    @Test
    void shouldCreateAuditEventId() {
        final AuditEventId auditEventId =
                new AuditEventId("event-001");

        assertEquals("event-001", auditEventId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new AuditEventId(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new AuditEventId("   "));
    }

    @Test
    void shouldCompareByValue() {
        final AuditEventId first =
                new AuditEventId("event-001");
        final AuditEventId second =
                new AuditEventId("event-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualOtherType() {
        final AuditEventId auditEventId =
                new AuditEventId("event-001");
        final String other = "event-001";

        assertNotEquals(auditEventId, other);
    }

    @Test
    void shouldReturnValueFromToString() {
        final AuditEventId auditEventId =
                new AuditEventId("event-001");

        assertEquals("event-001", auditEventId.toString());
    }
}

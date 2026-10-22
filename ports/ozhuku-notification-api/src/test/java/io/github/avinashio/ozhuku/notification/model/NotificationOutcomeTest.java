package io.github.avinashio.ozhuku.notification.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationOutcomeTest {

    @Test
    void shouldCreateSuccessfulOutcome() {
        final NotificationOutcome outcome =
                NotificationOutcome.success("msg-123");

        assertTrue(outcome.delivered());
        assertEquals("msg-123", outcome.messageId());
        assertNull(outcome.errorMessage());
    }

    @Test
    void shouldCreateFailureOutcome() {
        final NotificationOutcome outcome =
                NotificationOutcome.failure("SMTP unavailable");

        assertFalse(outcome.delivered());
        assertNull(outcome.messageId());
        assertEquals("SMTP unavailable", outcome.errorMessage());
    }
}

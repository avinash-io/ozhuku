package io.github.avinashio.ozhuku.notification.email;

import io.github.avinashio.ozhuku.notification.model.MailMessage;
import io.github.avinashio.ozhuku.notification.model.MimeContentType;
import io.github.avinashio.ozhuku.notification.model.NotificationOutcome;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryEmailNotificationProviderTest {

    private InMemoryEmailNotificationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new InMemoryEmailNotificationProvider();
    }

    @Test
    void shouldSendAndRecordMessage() {
        final MailMessage message = MailMessage.builder(
                List.of("recipient@example.com"),
                "Test Subject",
                "Test body")
                .build();

        final NotificationOutcome outcome = provider.send(message);

        assertTrue(outcome.delivered());
        assertNotNull(outcome.messageId());
        assertNull(outcome.errorMessage());
    }

    @Test
    void shouldRecordSentMessages() {
        final MailMessage message1 = MailMessage.builder(
                List.of("a@example.com"),
                "Subject 1",
                "Body 1")
                .build();

        final MailMessage message2 = MailMessage.builder(
                List.of("b@example.com"),
                "Subject 2",
                "Body 2")
                .contentType(MimeContentType.TEXT_HTML)
                .build();

        provider.send(message1);
        provider.send(message2);

        assertEquals(2, provider.sentCount());
        assertEquals(2, provider.sentMessages().size());
        assertEquals("Subject 1",
                provider.sentMessages().get(0).subject());
        assertEquals("Subject 2",
                provider.sentMessages().get(1).subject());
    }

    @Test
    void shouldReturnUnmodifiableList() {
        final MailMessage message = MailMessage.builder(
                List.of("recipient@example.com"),
                "Subject",
                "Body")
                .build();

        provider.send(message);

        assertThrows(
                UnsupportedOperationException.class,
                () -> provider.sentMessages().add(message));
    }

    @Test
    void shouldClearSentMessages() {
        final MailMessage message = MailMessage.builder(
                List.of("recipient@example.com"),
                "Subject",
                "Body")
                .build();

        provider.send(message);
        assertEquals(1, provider.sentCount());

        provider.clear();
        assertEquals(0, provider.sentCount());
    }

    @Test
    void shouldRejectNullMessage() {
        assertThrows(
                NullPointerException.class,
                () -> provider.send(null));
    }
}

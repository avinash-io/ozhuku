package io.github.avinashio.ozhuku.notification.email;

import io.github.avinashio.ozhuku.notification.EmailNotificationProvider;
import io.github.avinashio.ozhuku.notification.NotificationService;
import io.github.avinashio.ozhuku.notification.model.MailMessage;
import io.github.avinashio.ozhuku.notification.model.NotificationOutcome;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceFactoryTest {

    @Test
    void shouldCreateServiceFromProvider() {
        final InMemoryEmailNotificationProvider provider =
                new InMemoryEmailNotificationProvider();
        final NotificationService service =
                NotificationServiceFactory.create(provider);

        assertNotNull(service);
    }

    @Test
    void shouldDelegateToSendAndReturnSuccess() {
        final InMemoryEmailNotificationProvider provider =
                new InMemoryEmailNotificationProvider();
        final NotificationService service =
                NotificationServiceFactory.create(provider);

        final MailMessage message = MailMessage.builder(
                List.of("recipient@example.com"),
                "Test",
                "Body")
                .build();

        final NotificationOutcome outcome = service.notifyByEmail(message);

        assertTrue(outcome.delivered());
        assertNotNull(outcome.messageId());
        assertEquals(1, provider.sentCount());
    }

    @Test
    void shouldCaptureExceptionsAsFailureOutcome() {
        final EmailNotificationProvider failingProvider =
                message -> {
                    throw new RuntimeException("SMTP unavailable");
                };

        final NotificationService service =
                NotificationServiceFactory.create(failingProvider);

        final MailMessage message = MailMessage.builder(
                List.of("recipient@example.com"),
                "Test",
                "Body")
                .build();

        final NotificationOutcome outcome = service.notifyByEmail(message);

        assertFalse(outcome.delivered());
        assertNotNull(outcome.errorMessage());
        assertEquals("SMTP unavailable", outcome.errorMessage());
    }

    @Test
    void shouldRejectNullProvider() {
        assertThrows(
                NullPointerException.class,
                () -> NotificationServiceFactory.create(null));
    }
}

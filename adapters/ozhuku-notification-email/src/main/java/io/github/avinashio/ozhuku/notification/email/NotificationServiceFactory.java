package io.github.avinashio.ozhuku.notification.email;

import io.github.avinashio.ozhuku.notification.EmailNotificationProvider;
import io.github.avinashio.ozhuku.notification.NotificationService;
import io.github.avinashio.ozhuku.notification.model.MailMessage;
import io.github.avinashio.ozhuku.notification.model.NotificationOutcome;
import java.util.Objects;

/**
 * Default {@link NotificationService} implementation.
 *
 * <p>Delegates email delivery to an injected
 * {@link EmailNotificationProvider}. Notification failures are
 * captured in the {@link NotificationOutcome} and never propagated
 * to the caller, ensuring that notification failures do not fail
 * the ETL execution.</p>
 */
public final class NotificationServiceFactory {

    private NotificationServiceFactory() {
        // Utility class.
    }

    /**
     * Creates a notification service that delegates to the given provider.
     *
     * @param provider the email notification provider
     * @return a notification service
     */
    public static NotificationService create(
            final EmailNotificationProvider provider) {

        Objects.requireNonNull(provider, "provider must not be null");

        return new NotificationService() {
            @Override
            public NotificationOutcome notifyByEmail(
                    final MailMessage message) {

                try {
                    return provider.send(message);
                } catch (final Exception exception) {
                    return NotificationOutcome.failure(
                            exception.getMessage());
                }
            }
        };
    }
}

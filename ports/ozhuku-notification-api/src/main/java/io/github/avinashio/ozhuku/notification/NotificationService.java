package io.github.avinashio.ozhuku.notification;

import io.github.avinashio.ozhuku.notification.model.MailMessage;
import io.github.avinashio.ozhuku.notification.model.NotificationOutcome;

/**
 * Notification service for Ozhuku execution alerts.
 *
 * <p>The notification service is invoked when an execution completes,
 * fails, or requires attention. It delegates to one or more
 * {@link EmailNotificationProvider} instances configured per
 * notification profile. The notification unit is the
 * <strong>execution</strong>, not each individual file.</p>
 *
 * <p>Notification failure must never fail the ETL execution.</p>
 */
public interface NotificationService {

    /**
     * Sends a notification via email.
     *
     * @param message the email message to send
     * @return the notification outcome
     */
    NotificationOutcome notifyByEmail(MailMessage message);
}

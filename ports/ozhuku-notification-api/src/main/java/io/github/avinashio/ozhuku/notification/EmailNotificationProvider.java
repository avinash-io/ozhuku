package io.github.avinashio.ozhuku.notification;

import io.github.avinashio.ozhuku.notification.model.MailMessage;
import io.github.avinashio.ozhuku.notification.model.NotificationOutcome;

/**
 * Sends email notifications via an SMTP mail server.
 *
 * <p>This interface abstracts email delivery so that the notification
 * service can send alerts without depending on any specific mail
 * library. Implementations live in adapter modules.</p>
 *
 * <p>Notification failure must never fail the ETL execution. Callers
 * should handle failed outcomes gracefully and continue processing.</p>
 */
public interface EmailNotificationProvider {

    /**
     * Sends an email message.
     *
     * @param message the email message to send
     * @return the notification outcome
     */
    NotificationOutcome send(MailMessage message);
}

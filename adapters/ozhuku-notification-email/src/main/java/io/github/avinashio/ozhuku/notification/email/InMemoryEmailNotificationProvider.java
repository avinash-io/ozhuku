package io.github.avinashio.ozhuku.notification.email;

import io.github.avinashio.ozhuku.notification.EmailNotificationProvider;
import io.github.avinashio.ozhuku.notification.model.MailMessage;
import io.github.avinashio.ozhuku.notification.model.NotificationOutcome;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * In-memory email notification provider for testing and development.
 *
 * <p>This provider does not actually send emails. Instead, it records
 * all sent messages in an in-memory list that can be inspected by tests
 * or diagnostic endpoints. The provider assigns a random UUID as the
 * message ID for each successfully "sent" message.</p>
 *
 * <p>This implementation allows Ozhuku to be fully functional without
 * an SMTP server, and serves as a test double for the
 * {@link EmailNotificationProvider} contract.</p>
 */
public final class InMemoryEmailNotificationProvider
        implements EmailNotificationProvider {

    private final List<MailMessage> sentMessages;

    /**
     * Creates an in-memory email notification provider.
     */
    public InMemoryEmailNotificationProvider() {
        this.sentMessages = new ArrayList<>();
    }

    @Override
    public NotificationOutcome send(final MailMessage message) {
        Objects.requireNonNull(message, "message must not be null");

        sentMessages.add(message);

        return NotificationOutcome.success(UUID.randomUUID().toString());
    }

    /**
     * Returns an unmodifiable view of all sent messages.
     *
     * @return sent messages list
     */
    public List<MailMessage> sentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }

    /**
     * Returns the number of messages sent.
     *
     * @return sent message count
     */
    public int sentCount() {
        return sentMessages.size();
    }

    /**
     * Clears all sent messages.
     */
    public void clear() {
        sentMessages.clear();
    }
}

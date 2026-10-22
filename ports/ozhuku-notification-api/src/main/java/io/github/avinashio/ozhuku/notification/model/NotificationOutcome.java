package io.github.avinashio.ozhuku.notification.model;

import java.util.Objects;

/**
 * Represents the outcome of a notification delivery attempt.
 */
public final class NotificationOutcome {

    private final boolean delivered;

    private final String messageId;

    private final String errorMessage;

    private NotificationOutcome(
            final boolean delivered,
            final String messageId,
            final String errorMessage) {

        this.delivered = delivered;
        this.messageId = messageId;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful notification outcome.
     *
     * @param messageId the message identifier assigned by the mail server
     * @return a successful outcome
     */
    public static NotificationOutcome success(
            final String messageId) {

        return new NotificationOutcome(
                true,
                Objects.requireNonNull(messageId, "messageId must not be null"),
                null);
    }

    /**
     * Creates a failed notification outcome.
     *
     * @param errorMessage the error message
     * @return a failed outcome
     */
    public static NotificationOutcome failure(
            final String errorMessage) {

        return new NotificationOutcome(
                false,
                null,
                Objects.requireNonNull(errorMessage, "errorMessage must not be null"));
    }

    /**
     * Returns whether the notification was delivered successfully.
     *
     * @return {@code true} if delivered
     */
    public boolean delivered() {
        return delivered;
    }

    /**
     * Returns the message identifier.
     *
     * @return message ID, or {@code null} if delivery failed
     */
    public String messageId() {
        return messageId;
    }

    /**
     * Returns the error message.
     *
     * @return error message, or {@code null} if delivery succeeded
     */
    public String errorMessage() {
        return errorMessage;
    }
}

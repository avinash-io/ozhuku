package io.github.avinashio.ozhuku.notification.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents an email message for notification delivery.
 */
public final class MailMessage {

    private final List<String> to;

    private final List<String> cc;

    private final String subject;

    private final String body;

    private final MimeContentType contentType;

    private final List<Attachment> attachments;

    private MailMessage(
            final List<String> to,
            final List<String> cc,
            final String subject,
            final String body,
            final MimeContentType contentType,
            final List<Attachment> attachments) {

        this.to = List.copyOf(
                Objects.requireNonNull(to, "to must not be null"));
        this.cc = List.copyOf(
                Objects.requireNonNull(cc, "cc must not be null"));
        this.subject = Objects.requireNonNull(
                subject,
                "subject must not be null");
        this.body = Objects.requireNonNull(
                body,
                "body must not be null");
        this.contentType = Objects.requireNonNull(
                contentType,
                "contentType must not be null");
        this.attachments = List.copyOf(
                Objects.requireNonNull(
                        attachments,
                        "attachments must not be null"));
    }

    /**
     * Creates a builder for a {@link MailMessage}.
     *
     * @param to      the recipient addresses
     * @param subject the email subject
     * @param body    the email body
     * @return a new builder
     */
    public static Builder builder(
            final List<String> to,
            final String subject,
            final String body) {

        return new Builder(to, subject, body);
    }

    /**
     * Returns the recipient addresses.
     *
     * @return recipient list
     */
    public List<String> to() {
        return to;
    }

    /**
     * Returns the CC addresses.
     *
     * @return CC list
     */
    public List<String> cc() {
        return cc;
    }

    /**
     * Returns the subject.
     *
     * @return subject
     */
    public String subject() {
        return subject;
    }

    /**
     * Returns the body.
     *
     * @return body
     */
    public String body() {
        return body;
    }

    /**
     * Returns the content type.
     *
     * @return content type
     */
    public MimeContentType contentType() {
        return contentType;
    }

    /**
     * Returns the attachments.
     *
     * @return attachment list
     */
    public List<Attachment> attachments() {
        return attachments;
    }

    /**
     * Builder for {@link MailMessage}.
     */
    public static final class Builder {

        private final List<String> to;

        private final String subject;

        private final String body;

        private List<String> cc = List.of();

        private MimeContentType contentType = MimeContentType.TEXT_PLAIN;

        private List<Attachment> attachments = List.of();

        private Builder(
                final List<String> to,
                final String subject,
                final String body) {

            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        /**
         * Sets the CC addresses.
         *
         * @param cc the CC addresses
         * @return this builder
         */
        public Builder cc(final List<String> cc) {
            this.cc = cc;
            return this;
        }

        /**
         * Sets the content type.
         *
         * @param contentType the content type
         * @return this builder
         */
        public Builder contentType(final MimeContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Sets the attachments.
         *
         * @param attachments the attachments
         * @return this builder
         */
        public Builder attachments(final List<Attachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        /**
         * Builds the mail message.
         *
         * @return a new {@link MailMessage}
         */
        public MailMessage build() {
            return new MailMessage(
                    to,
                    cc,
                    subject,
                    body,
                    contentType,
                    attachments);
        }
    }
}

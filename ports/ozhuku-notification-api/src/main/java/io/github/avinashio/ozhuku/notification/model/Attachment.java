package io.github.avinashio.ozhuku.notification.model;

import java.util.Objects;

/**
 * Represents an email attachment.
 */
public final class Attachment {

    private final String filename;

    private final byte[] content;

    private final String contentType;

    /**
     * Creates an attachment.
     *
     * @param filename    the attachment filename
     * @param content     the attachment content as bytes
     * @param contentType the content type (e.g. "text/csv")
     */
    public Attachment(
            final String filename,
            final byte[] content,
            final String contentType) {

        this.filename = Objects.requireNonNull(
                filename,
                "filename must not be null");
        this.content = Objects.requireNonNull(
                content,
                "content must not be null");
        this.contentType = Objects.requireNonNull(
                contentType,
                "contentType must not be null");
    }

    /**
     * Returns the filename.
     *
     * @return filename
     */
    public String filename() {
        return filename;
    }

    /**
     * Returns the content.
     *
     * @return content bytes
     */
    public byte[] content() {
        return content;
    }

    /**
     * Returns the content type.
     *
     * @return content type
     */
    public String contentType() {
        return contentType;
    }
}

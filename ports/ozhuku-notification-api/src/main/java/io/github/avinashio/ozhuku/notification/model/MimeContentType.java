package io.github.avinashio.ozhuku.notification.model;

/**
 * MIME content types supported for notification messages.
 */
public enum MimeContentType {

    /**
     * Plain text content.
     */
    TEXT_PLAIN("text/plain"),

    /**
     * HTML content.
     */
    TEXT_HTML("text/html");

    private final String mimeType;

    MimeContentType(final String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the MIME type string.
     *
     * @return MIME type
     */
    public String mimeType() {
        return mimeType;
    }
}

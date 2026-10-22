package io.github.avinashio.ozhuku.transport.soap;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Represents a SOAP message envelope for transmission over HTTP.
 *
 * <p>This class encapsulates the SOAP envelope content, the SOAP action
 * header (when present), and the target URI. It is intentionally minimal:
 * Ozhuku does not implement a full SOAP stack. The envelope body is
 * treated as opaque XML content provided by the caller.</p>
 *
 * <p>The SOAP 1.2 specification defines the {@code application/soap+xml}
 * media type and the {@code SOAPAction} header. This class supports both
 * SOAP 1.1 ({@code text/xml} with {@code SOAPAction}) and SOAP 1.2
 * ({@code application/soap+xml}).</p>
 */
public final class SoapMessage {

    private final String envelope;
    private final String soapAction;
    private final String contentType;

    private SoapMessage(
            final String envelope,
            final String soapAction,
            final String contentType) {

        this.envelope = Validation.requireNonNull(
                envelope,
                "envelope must not be null");
        this.soapAction = soapAction;
        this.contentType = Validation.requireNonNull(
                contentType,
                "contentType must not be null");
    }

    /**
     * Creates a SOAP 1.1 message.
     *
     * <p>SOAP 1.1 uses the {@code text/xml} content type and includes
     * a {@code SOAPAction} header.</p>
     *
     * @param envelope the SOAP envelope XML content
     * @param soapAction the SOAP action header value (may be empty string)
     * @return a SOAP 1.1 message
     */
    public static SoapMessage soap11(
            final String envelope,
            final String soapAction) {

        return new SoapMessage(
                envelope,
                soapAction,
                "text/xml; charset=utf-8");
    }

    /**
     * Creates a SOAP 1.2 message.
     *
     * <p>SOAP 1.2 uses the {@code application/soap+xml} content type.
     * The SOAP action is conveyed via the {@code SOAPAction} header
     * or the {@code action} parameter in the content type.</p>
     *
     * @param envelope the SOAP envelope XML content
     * @param soapAction the SOAP action URI (may be {@code null})
     * @return a SOAP 1.2 message
     */
    public static SoapMessage soap12(
            final String envelope,
            final String soapAction) {

        final String resolvedContentType = soapAction != null
                ? "application/soap+xml; charset=utf-8; action=\""
                        + soapAction + "\""
                : "application/soap+xml; charset=utf-8";

        return new SoapMessage(
                envelope,
                soapAction,
                resolvedContentType);
    }

    /**
     * Creates a message with explicit content type.
     *
     * @param envelope the SOAP envelope XML content
     * @param soapAction the SOAP action (may be {@code null})
     * @param contentType the content type for the HTTP request
     * @return a SOAP message
     */
    public static SoapMessage of(
            final String envelope,
            final String soapAction,
            final String contentType) {

        return new SoapMessage(envelope, soapAction, contentType);
    }

    /**
     * Returns the SOAP envelope content.
     *
     * @return envelope XML
     */
    public String envelope() {
        return envelope;
    }

    /**
     * Returns the SOAP action header value, or {@code null} if not set.
     *
     * @return SOAP action
     */
    public String soapAction() {
        return soapAction;
    }

    /**
     * Returns the content type for the HTTP request.
     *
     * @return content type
     */
    public String contentType() {
        return contentType;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SoapMessage)) {
            return false;
        }

        final SoapMessage that = (SoapMessage) other;
        return envelope.equals(that.envelope)
                && Objects.equals(soapAction, that.soapAction)
                && contentType.equals(that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(envelope, soapAction, contentType);
    }

    @Override
    public String toString() {
        return "SoapMessage{"
                + "contentType=" + contentType
                + ", soapAction=" + soapAction
                + '}';
    }
}

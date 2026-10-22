package io.github.avinashio.ozhuku.format.xml;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Defines configuration used by the Ozhuku XML reader.
 *
 * <p>XML input is expected to consist of repeated record elements. By default
 * each top-level child element of the root element is treated as a single
 * record, and each child element within a record element becomes a named
 * field. The reader is streaming (pull-parser based) so memory usage stays
 * bounded regardless of input size.
 */
public final class XmlReaderConfiguration {

    private static final Charset DEFAULT_CHARSET =
            StandardCharsets.UTF_8;

    private static final String DEFAULT_RECORD_ELEMENT = "record";

    private final Charset charset;
    private final String recordElement;

    private XmlReaderConfiguration(
            final Charset charset,
            final String recordElement) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");

        this.recordElement = Objects.requireNonNull(
                recordElement,
                "recordElement must not be null");
    }

    public static XmlReaderConfiguration defaults() {
        return new XmlReaderConfiguration(
                DEFAULT_CHARSET,
                DEFAULT_RECORD_ELEMENT);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Charset charset() {
        return charset;
    }

    public String recordElement() {
        return recordElement;
    }

    public static final class Builder {

        private Charset charset = DEFAULT_CHARSET;
        private String recordElement = DEFAULT_RECORD_ELEMENT;

        public Builder charset(
                final Charset charset) {

            this.charset = Objects.requireNonNull(
                    charset,
                    "charset must not be null");

            return this;
        }

        public Builder recordElement(
                final String recordElement) {

            this.recordElement = Objects.requireNonNull(
                    recordElement,
                    "recordElement must not be null");

            return this;
        }

        public XmlReaderConfiguration build() {
            return new XmlReaderConfiguration(
                    charset,
                    recordElement);
        }
    }
}

package io.github.avinashio.ozhuku.format.xml;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Defines configuration used by the Ozhuku XML writer.
 *
 * <p>By default records are emitted as child elements of a root element,
 * with each record as &lt;record&gt; and each field as a named child element.
 * The writer supports pretty-printing for readability.
 */
public final class XmlWriterConfiguration {

    private static final Charset DEFAULT_CHARSET =
            StandardCharsets.UTF_8;

    private static final String DEFAULT_ROOT_ELEMENT = "records";

    private static final String DEFAULT_RECORD_ELEMENT = "record";

    private static final boolean DEFAULT_PRETTY_PRINT =
            false;

    private final Charset charset;
    private final String rootElement;
    private final String recordElement;
    private final boolean prettyPrint;

    private XmlWriterConfiguration(
            final Charset charset,
            final String rootElement,
            final String recordElement,
            final boolean prettyPrint) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");

        this.rootElement = Objects.requireNonNull(
                rootElement,
                "rootElement must not be null");

        this.recordElement = Objects.requireNonNull(
                recordElement,
                "recordElement must not be null");

        this.prettyPrint = prettyPrint;
    }

    public static XmlWriterConfiguration defaults() {
        return new XmlWriterConfiguration(
                DEFAULT_CHARSET,
                DEFAULT_ROOT_ELEMENT,
                DEFAULT_RECORD_ELEMENT,
                DEFAULT_PRETTY_PRINT);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Charset charset() {
        return charset;
    }

    public String rootElement() {
        return rootElement;
    }

    public String recordElement() {
        return recordElement;
    }

    public boolean prettyPrint() {
        return prettyPrint;
    }

    public static final class Builder {

        private Charset charset = DEFAULT_CHARSET;
        private String rootElement = DEFAULT_ROOT_ELEMENT;
        private String recordElement = DEFAULT_RECORD_ELEMENT;
        private boolean prettyPrint = DEFAULT_PRETTY_PRINT;

        public Builder charset(
                final Charset charset) {

            this.charset = Objects.requireNonNull(
                    charset,
                    "charset must not be null");

            return this;
        }

        public Builder rootElement(
                final String rootElement) {

            this.rootElement = Objects.requireNonNull(
                    rootElement,
                    "rootElement must not be null");

            return this;
        }

        public Builder recordElement(
                final String recordElement) {

            this.recordElement = Objects.requireNonNull(
                    recordElement,
                    "recordElement must not be null");

            return this;
        }

        public Builder prettyPrint(
                final boolean prettyPrint) {

            this.prettyPrint = prettyPrint;
            return this;
        }

        public XmlWriterConfiguration build() {
            return new XmlWriterConfiguration(
                    charset,
                    rootElement,
                    recordElement,
                    prettyPrint);
        }
    }
}

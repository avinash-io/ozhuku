package io.github.avinashio.ozhuku.format.xml;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Writes format-neutral Ozhuku records as XML.
 *
 * <p>Each record is emitted as a child element of a configurable root
 * element (default &lt;records&gt;). Each field in the record becomes a
 * named child element whose text content is the field value.
 *
 * <p>The writer uses a streaming StAX cursor writer so memory usage stays
 * bounded regardless of record count.
 */
public final class XmlFormatWriter implements FormatWriter {

    private final XmlWriterConfiguration configuration;

    private BufferedOutputStream buffered;
    private XMLStreamWriter writer;
    private String rootElement;
    private String recordElement;
    private boolean opened;
    private boolean rootStarted;

    public XmlFormatWriter(
            final XmlWriterConfiguration configuration) {

        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null");
    }

    @Override
    public void open(
            final OutputStream outputStream)
            throws IOException {

        Objects.requireNonNull(
                outputStream,
                "outputStream must not be null");

        if (opened) {
            throw new IllegalStateException(
                    "XML writer is already open");
        }

        final Charset charset =
                configuration.charset();

        buffered = new BufferedOutputStream(outputStream);

        final XMLOutputFactory factory =
                XMLOutputFactory.newInstance();

        try {
            writer = factory.createXMLStreamWriter(
                    buffered,
                    charset.name());
        } catch (final XMLStreamException exception) {
            throw new IOException(
                    "Failed to create XML stream writer",
                    exception);
        }

        rootElement = configuration.rootElement();
        recordElement = configuration.recordElement();
        opened = true;
        rootStarted = false;
    }

    @Override
    public void write(
            final Record record)
            throws IOException {

        Objects.requireNonNull(
                record,
                "record must not be null");

        if (!opened) {
            throw new IllegalStateException(
                    "XML writer must be opened before writing");
        }

        if (!rootStarted) {
            try {
                writer.writeStartDocument(
                        configuration.charset().name(),
                        "1.0");

                writer.writeStartElement(rootElement);
                rootStarted = true;

                if (configuration.prettyPrint()) {
                    writer.writeCharacters("\n");
                }
            } catch (final XMLStreamException exception) {
                throw new IOException(
                        "Failed to start XML document",
                        exception);
            }
        }

        try {
            if (configuration.prettyPrint()) {
                writer.writeCharacters("  ");
            }

            if (record.fields().isEmpty()) {
                writer.writeEmptyElement(recordElement);
            } else {
                writer.writeStartElement(recordElement);

                for (final RecordField field : record.fields()) {
                    writeField(field);
                }

                writer.writeEndElement();
            }

            if (configuration.prettyPrint()) {
                writer.writeCharacters("\n");
            }
        } catch (final XMLStreamException exception) {
            throw new IOException(
                    "Failed to write XML record",
                    exception);
        }
    }

    private void writeField(
            final RecordField field)
            throws XMLStreamException {

        final Object value =
                field.value();

        final RecordValueType type =
                field.type();

        if (value == null
                || type == RecordValueType.NULL) {

            writer.writeEmptyElement(field.name());
            writer.writeAttribute("nil", "true");

        } else {
            writer.writeStartElement(field.name());

            if (type == RecordValueType.BOOLEAN) {
                if (value instanceof Boolean booleanValue) {
                    writer.writeCharacters(
                            String.valueOf(booleanValue));
                } else {
                    writer.writeCharacters(
                            String.valueOf(value));
                }

            } else if (type == RecordValueType.INTEGER) {
                if (value instanceof Number number) {
                    writer.writeCharacters(
                            String.valueOf(number.longValue()));
                } else {
                    writer.writeCharacters(
                            String.valueOf(value));
                }

            } else if (type == RecordValueType.DECIMAL) {
                if (value instanceof Number number) {
                    writer.writeCharacters(
                            String.valueOf(number.doubleValue()));
                } else {
                    writer.writeCharacters(
                            String.valueOf(value));
                }

            } else if (type == RecordValueType.DATE
                    || type == RecordValueType.TIME
                    || type == RecordValueType.DATETIME) {
                writer.writeCharacters(
                        String.valueOf(value));

            } else if (type == RecordValueType.BINARY) {
                if (value instanceof byte[] bytes) {
                    writer.writeCharacters(
                            java.util.Base64.getEncoder()
                                    .encodeToString(bytes));
                } else {
                    writer.writeCharacters(
                            String.valueOf(value));
                }

            } else {
                writer.writeCharacters(
                        String.valueOf(value));
            }

            writer.writeEndElement();
        }
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (opened && rootStarted) {
            try {
                if (configuration.prettyPrint()) {
                    writer.writeCharacters("");
                }

                writer.writeEndElement();
                writer.writeEndDocument();
            } catch (final XMLStreamException exception) {
                failure = new IOException(
                        "Failed to close XML document",
                        exception);
            }
        }

        if (writer != null) {
            try {
                writer.flush();
            } catch (final XMLStreamException exception) {
                if (failure == null) {
                    failure = new IOException(
                            "Failed to flush XML writer",
                            exception);
                } else {
                    failure.addSuppressed(exception);
                }
            }

            try {
                writer.close();
            } catch (final XMLStreamException exception) {
                if (failure == null) {
                    failure = new IOException(
                            "Failed to close XML writer",
                            exception);
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        if (buffered != null) {
            try {
                buffered.close();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        buffered = null;
        writer = null;
        rootElement = null;
        recordElement = null;
        opened = false;
        rootStarted = false;

        if (failure != null) {
            throw failure;
        }
    }
}

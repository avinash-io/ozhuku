package io.github.avinashio.ozhuku.format.xml;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Reads XML input as format-neutral Ozhuku records.
 *
 * <p>Each occurrence of the configured record element (default
 * &lt;record&gt;) becomes a single Ozhuku record. Each child element within
 * a record element becomes a named field, and the element text content is
 * used as the field value. Values are typed heuristically: boolean literals
 * ("true"/"false") are typed as {@link RecordValueType#BOOLEAN}, numeric
 * strings as {@link RecordValueType#INTEGER} or
 * {@link RecordValueType#DECIMAL}, and everything else as
 * {@link RecordValueType#STRING}.
 *
 * <p>The reader uses a streaming StAX pull parser so memory usage stays
 * bounded regardless of input size.
 */
public final class XmlFormatReader implements FormatReader {

    private final XmlReaderConfiguration configuration;

    private XMLStreamReader reader;
    private String recordElement;
    private long sequence;
    private boolean opened;
    private boolean endOfInput;

    public XmlFormatReader(
            final XmlReaderConfiguration configuration) {

        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null");
    }

    @Override
    public void open(
            final InputStream inputStream)
            throws IOException {

        Objects.requireNonNull(
                inputStream,
                "inputStream must not be null");

        if (opened) {
            throw new IllegalStateException(
                    "XML reader is already open");
        }

        final Charset charset =
                configuration.charset();

        final XMLInputFactory factory =
                XMLInputFactory.newInstance();

        factory.setProperty(
                XMLInputFactory.IS_COALESCING,
                true);

        try {
            reader = factory.createXMLStreamReader(
                    new BufferedInputStream(inputStream),
                    charset.name());
        } catch (final XMLStreamException exception) {
            throw new IOException(
                    "Failed to create XML stream reader",
                    exception);
        }

        recordElement = configuration.recordElement();
        sequence = 0;
        endOfInput = false;
        opened = true;
    }

    @Override
    public FormatReadResult read()
            throws IOException {

        if (!opened) {
            throw new IllegalStateException(
                    "XML reader must be opened before reading");
        }

        if (endOfInput) {
            return FormatReadResult.endOfInput();
        }

        // Advance to the next START_ELEMENT matching the record element.
        while (true) {
            try {
                if (!reader.hasNext()) {
                    endOfInput = true;
                    return FormatReadResult.endOfInput();
                }

                reader.next();

                if (reader.isStartElement()
                        && recordElement.equals(
                                reader.getLocalName())) {

                    return readRecord();
                }
            } catch (final XMLStreamException exception) {
                // Empty or malformed input with no records is treated as
                // end-of-input rather than an error.
                endOfInput = true;
                return FormatReadResult.endOfInput();
            }
        }
    }

    private FormatReadResult readRecord()
            throws IOException {

        final List<RecordField> fields =
                new ArrayList<>();

        try {
            while (reader.hasNext()) {
                int event = reader.next();

                if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT
                        && recordElement.equals(
                                reader.getLocalName())) {
                    break;
                }

                if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
                    final String fieldName =
                            reader.getLocalName();

                    final String textValue =
                            reader.getElementText();

                    final Object fieldValue =
                            convertValue(textValue);

                    fields.add(
                            new RecordField(
                                    fieldName,
                                    inferType(textValue),
                                    fieldValue));
                }
            }
        } catch (final XMLStreamException exception) {
            throw new IOException(
                    "Failed to parse XML record",
                    exception);
        }

        final Record record =
                new Record(
                        sequence++,
                        fields,
                        RecordMetadata.empty());

        return FormatReadResult.record(record);
    }

    private RecordValueType inferType(
            final String value) {

        if (value == null) {
            return RecordValueType.NULL;
        }

        final String trimmed =
                value.trim();

        if (trimmed.isEmpty()) {
            return RecordValueType.NULL;
        }

        if ("true".equalsIgnoreCase(trimmed)
                || "false".equalsIgnoreCase(trimmed)) {
            return RecordValueType.BOOLEAN;
        }

        try {
            Long.parseLong(trimmed);
            return RecordValueType.INTEGER;
        } catch (final NumberFormatException ignored) {
        }

        try {
            Double.parseDouble(trimmed);
            return RecordValueType.DECIMAL;
        } catch (final NumberFormatException ignored) {
        }

        return RecordValueType.STRING;
    }

    private Object convertValue(
            final String textValue) {

        if (textValue == null) {
            return null;
        }

        final String trimmed =
                textValue.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }

        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        try {
            return Long.parseLong(trimmed);
        } catch (final NumberFormatException ignored) {
        }

        try {
            return Double.parseDouble(trimmed);
        } catch (final NumberFormatException ignored) {
        }

        return textValue;
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (reader != null) {
            try {
                reader.close();
            } catch (final XMLStreamException exception) {
                failure = new IOException(
                        "Failed to close XML reader",
                        exception);
            }
        }

        reader = null;
        recordElement = null;
        opened = false;
        endOfInput = false;
        sequence = 0;

        if (failure != null) {
            throw failure;
        }
    }
}

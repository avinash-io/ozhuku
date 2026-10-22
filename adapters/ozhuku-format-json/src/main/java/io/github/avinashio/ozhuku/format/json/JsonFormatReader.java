package io.github.avinashio.ozhuku.format.json;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads JSON input as format-neutral Ozhuku records.
 *
 * Supports JSON arrays of objects and newline-delimited JSON (NDJSON).
 */
public final class JsonFormatReader implements FormatReader {

    private final JsonReaderConfiguration configuration;

    private JsonFactory factory;
    private JsonParser parser;
    private boolean opened;
    private long sequence;
    private boolean endOfInput;
    private boolean arrayStarted;

    public JsonFormatReader(
            final JsonReaderConfiguration configuration) {

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
                    "JSON reader is already open");
        }

        final Charset charset =
                configuration.charset();

        factory = new JsonFactory();

        parser = factory.createParser(
                inputStream);

        opened = true;
        sequence = 0;
        endOfInput = false;
        arrayStarted = false;
    }

    @Override
    public FormatReadResult read()
            throws IOException {

        if (!opened) {
            throw new IllegalStateException(
                    "JSON reader must be opened before reading");
        }

        if (endOfInput) {
            return FormatReadResult.endOfInput();
        }

        // Advance to the next start object.
        while (true) {
            final JsonToken token =
                    parser.nextToken();

            if (token == null) {
                endOfInput = true;
                return FormatReadResult.endOfInput();
            }

            if (token == JsonToken.START_ARRAY) {
                arrayStarted = true;
                continue;
            }

            if (token == JsonToken.END_ARRAY) {
                endOfInput = true;
                return FormatReadResult.endOfInput();
            }

            if (token == JsonToken.START_OBJECT) {
                return readObject();
            }

            if (token == JsonToken.END_OBJECT) {
                continue;
            }
        }
    }

    private FormatReadResult readObject()
            throws IOException {

        final List<RecordField> fields =
                new ArrayList<>();

        while (true) {
            final JsonToken token =
                    parser.nextToken();

            if (token == JsonToken.END_OBJECT) {
                break;
            }

            if (token != JsonToken.FIELD_NAME) {
                continue;
            }

            final String fieldName =
                    parser.getCurrentName();

            parser.nextToken();

            final RecordField field =
                    readFieldValue(
                            fieldName,
                            parser.currentToken());

            fields.add(field);
        }

        final Record record =
                new Record(
                        sequence++,
                        fields,
                        RecordMetadata.empty());

        return FormatReadResult.record(record);
    }

    private RecordField readFieldValue(
            final String name,
            final JsonToken token)
            throws IOException {

        if (token == JsonToken.VALUE_NULL) {
            return new RecordField(
                    name,
                    RecordValueType.NULL,
                    null);
        }

        if (token == JsonToken.VALUE_TRUE
                || token == JsonToken.VALUE_FALSE) {

            return new RecordField(
                    name,
                    RecordValueType.BOOLEAN,
                    parser.getBooleanValue());
        }

        if (token == JsonToken.VALUE_NUMBER_INT) {

            return new RecordField(
                    name,
                    RecordValueType.INTEGER,
                    parser.getLongValue());
        }

        if (token == JsonToken.VALUE_NUMBER_FLOAT) {

            return new RecordField(
                    name,
                    RecordValueType.DECIMAL,
                    parser.getDoubleValue());
        }

        if (token == JsonToken.VALUE_STRING) {
            return new RecordField(
                    name,
                    RecordValueType.STRING,
                    parser.getText());
        }

        // Structural tokens (START_OBJECT, START_ARRAY) not handled for nested values.
        return new RecordField(
                name,
                RecordValueType.STRING,
                parser.getText());
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (parser != null) {
            try {
                parser.close();
            } catch (final IOException exception) {
                failure = exception;
            }
        }

        if (factory != null) {
            // JsonFactory does not need explicit close.
        }

        parser = null;
        factory = null;
        opened = false;
        endOfInput = false;
        sequence = 0;
        arrayStarted = false;

        if (failure != null) {
            throw failure;
        }
    }
}

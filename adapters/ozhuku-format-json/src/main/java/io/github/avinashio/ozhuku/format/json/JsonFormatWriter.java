package io.github.avinashio.ozhuku.format.json;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatWriter;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Writes format-neutral Ozhuku records as JSON.
 *
 * Supports array-wrapped output (a JSON array of object records) and
 * newline-delimited JSON (one object per line) for streaming.
 */
public final class JsonFormatWriter implements FormatWriter {

    private final JsonWriterConfiguration configuration;

    private BufferedWriter writer;
    private JsonGenerator generator;
    private boolean opened;
    private boolean arrayStarted;
    private boolean headerWritten;

    public JsonFormatWriter(
            final JsonWriterConfiguration configuration) {

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
                    "JSON writer is already open");
        }

        final Charset charset =
                configuration.charset();

        writer = new BufferedWriter(
                new OutputStreamWriter(
                        outputStream,
                        charset));

        final ObjectMapper mapper =
                new ObjectMapper();

        if (configuration.prettyPrint()) {
            mapper.enable(
                    SerializationFeature.INDENT_OUTPUT);
        }

        generator = mapper.getFactory()
                .createGenerator(
                        outputStream,
                        JsonEncoding.UTF8);

        opened = true;
        arrayStarted = false;
        headerWritten = false;
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
                    "JSON writer must be opened before writing");
        }

        if (configuration.arrayWrapped()
                && !arrayStarted) {

            generator.writeStartArray();
            arrayStarted = true;
        }

        generator.writeStartObject();

        for (final RecordField field : record.fields()) {
            writeField(field);
        }

        generator.writeEndObject();
    }

    private void writeField(
            final RecordField field)
            throws IOException {

        generator.writeFieldName(field.name());

        final Object value =
                field.value();

        final RecordValueType type =
                field.type();

        if (value == null
                || type == RecordValueType.NULL) {

            generator.writeNull();

        } else if (type == RecordValueType.STRING) {
            generator.writeString(
                    String.valueOf(value));

        } else if (type == RecordValueType.INTEGER) {
            if (value instanceof Number) {
                generator.writeNumber(
                        ((Number) value).longValue());
            } else {
                generator.writeNumber(
                        Long.parseLong(
                                String.valueOf(value)));
            }

        } else if (type == RecordValueType.DECIMAL) {
            if (value instanceof Number) {
                generator.writeNumber(
                        ((Number) value).doubleValue());
            } else {
                generator.writeNumber(
                        Double.parseDouble(
                                String.valueOf(value)));
            }

        } else if (type == RecordValueType.BOOLEAN) {
            if (value instanceof Boolean) {
                generator.writeBoolean(
                        (Boolean) value);
            } else {
                generator.writeBoolean(
                        Boolean.parseBoolean(
                                String.valueOf(value)));
            }

        } else if (type == RecordValueType.DATE
                || type == RecordValueType.TIME
                || type == RecordValueType.DATETIME) {
            generator.writeString(
                    String.valueOf(value));

        } else if (type == RecordValueType.BINARY) {
            if (value instanceof byte[]) {
                generator.writeBinary(
                        (byte[]) value);
            } else {
                generator.writeString(
                        String.valueOf(value));
            }

        } else {
            generator.writeString(
                    String.valueOf(value));
        }
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (opened) {
            if (arrayStarted) {
                try {
                    generator.writeEndArray();
                } catch (final IOException exception) {
                    failure = exception;
                }
            }
        }

        if (generator != null) {
            try {
                generator.flush();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }

            try {
                generator.close();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        generator = null;
        writer = null;
        opened = false;
        arrayStarted = false;
        headerWritten = false;

        if (failure != null) {
            throw failure;
        }
    }
}

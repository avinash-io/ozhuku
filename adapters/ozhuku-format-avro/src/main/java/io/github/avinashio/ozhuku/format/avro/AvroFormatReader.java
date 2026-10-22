package io.github.avinashio.ozhuku.format.avro;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads Avro input as format-neutral Ozhuku records.
 *
 * <p>The reader uses Avro's DataFileReader to stream records from an
 * Avro file. The Avro file's embedded schema is used to decode the
 * records. Each Avro record becomes a single Ozhuku record, with each
 * field in the Avro record becoming a named field in the Ozhuku record.
 *
 * <p>The reader is streaming so memory usage stays bounded regardless of
 * input size.
 */
public final class AvroFormatReader implements FormatReader {

    private final AvroReaderConfiguration configuration;

    private DataFileStream<GenericRecord> reader;
    private Schema schema;
    private long sequence;
    private boolean opened;
    private boolean endOfInput;

    public AvroFormatReader(
            final AvroReaderConfiguration configuration) {

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
                    "Avro reader is already open");
        }

        final DatumReader<GenericRecord> datumReader =
                new GenericDatumReader<>();

        reader = new DataFileStream<>(
                inputStream,
                datumReader);

        schema = reader.getSchema();
        sequence = 0;
        endOfInput = false;
        opened = true;
    }

    @Override
    public FormatReadResult read()
            throws IOException {

        if (!opened) {
            throw new IllegalStateException(
                    "Avro reader must be opened before reading");
        }

        if (endOfInput) {
            return FormatReadResult.endOfInput();
        }

        if (!reader.hasNext()) {
            endOfInput = true;
            return FormatReadResult.endOfInput();
        }

        final GenericRecord avroRecord = reader.next();
        final List<RecordField> fields = new ArrayList<>();

        for (final Schema.Field field : schema.getFields()) {
            final String fieldName = field.name();
            final Object value = avroRecord.get(fieldName);

            fields.add(convertField(fieldName, field.schema(), value));
        }

        final Record record =
                new Record(
                        sequence++,
                        fields,
                        RecordMetadata.empty());

        return FormatReadResult.record(record);
    }

    private RecordField convertField(
            final String name,
            final Schema schema,
            final Object value) {

        if (value == null) {
            return new RecordField(
                    name,
                    RecordValueType.NULL,
                    null);
        }

        // Handle union types (e.g., ["null", "boolean"])
        if (schema.getType() == Schema.Type.UNION) {
            for (final Schema branch : schema.getTypes()) {
                if (branch.getType() != Schema.Type.NULL) {
                    return convertField(name, branch, value);
                }
            }
            // All branches are NULL
            return new RecordField(name, RecordValueType.NULL, null);
        }

        switch (schema.getType()) {
            case BOOLEAN:
                return new RecordField(
                        name,
                        RecordValueType.BOOLEAN,
                        value);

            case INT:
                return new RecordField(
                        name,
                        RecordValueType.INTEGER,
                        ((Number) value).longValue());

            case LONG:
                return new RecordField(
                        name,
                        RecordValueType.INTEGER,
                        ((Number) value).longValue());

            case FLOAT:
                return new RecordField(
                        name,
                        RecordValueType.DECIMAL,
                        ((Number) value).doubleValue());

            case DOUBLE:
                return new RecordField(
                        name,
                        RecordValueType.DECIMAL,
                        ((Number) value).doubleValue());

            case STRING:
                return new RecordField(
                        name,
                        RecordValueType.STRING,
                        value.toString());

            case BYTES:
                if (value instanceof org.apache.avro.util.Utf8) {
                    return new RecordField(
                            name,
                            RecordValueType.STRING,
                            value.toString());
                }
                if (value instanceof java.nio.ByteBuffer) {
                    final java.nio.ByteBuffer buffer = (java.nio.ByteBuffer) value;
                    final byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    return new RecordField(
                            name,
                            RecordValueType.BINARY,
                            bytes);
                }
                return new RecordField(
                        name,
                        RecordValueType.BINARY,
                        value);

            case RECORD:
            case ARRAY:
            case MAP:
            case ENUM:
            case FIXED:
            case NULL:
            default:
                return new RecordField(
                        name,
                        RecordValueType.STRING,
                        value.toString());
        }
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException exception) {
                failure = exception;
            }
        }

        reader = null;
        schema = null;
        opened = false;
        endOfInput = false;
        sequence = 0;

        if (failure != null) {
            throw failure;
        }
    }
}
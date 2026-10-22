package io.github.avinashio.ozhuku.format.avro;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatWriter;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DatumWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Writes format-neutral Ozhuku records as Avro.
 *
 * <p>The writer requires a schema to write Avro records. The schema can
 * be provided via configuration (as a JSON string) or inferred from the
 * first record written. The writer uses Avro's DataFileWriter to stream
 * records to the output, so memory usage stays bounded regardless of
 * record count.
 */
public final class AvroFormatWriter implements FormatWriter {

    private final AvroWriterConfiguration configuration;

    private DataFileWriter<GenericRecord> writer;
    private Schema schema;
    private OutputStream outputStream;
    private boolean opened;
    private boolean schemaInitialized;

    public AvroFormatWriter(
            final AvroWriterConfiguration configuration) {

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
                    "Avro writer is already open");
        }

        // If schema is provided in configuration, use it
        if (configuration.schemaJson() != null) {
            schema = new Schema.Parser().parse(configuration.schemaJson());
        }

        final DatumWriter<GenericRecord> datumWriter =
                new GenericDatumWriter<>();

        writer = new DataFileWriter<>(datumWriter);

        if (configuration.compressionCodec() != null
                && !configuration.compressionCodec().isEmpty()) {
            writer.setCodec(CodecFactory.fromString(configuration.compressionCodec()));
        }

        if (schema != null) {
            writer.create(schema, outputStream);
        }

        this.outputStream = outputStream;
        opened = true;
        schemaInitialized = (schema != null);
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
                    "Avro writer must be opened before writing");
        }

        // Initialize schema from first record if not provided
        if (!schemaInitialized) {
            schema = inferSchema(record);
            writer.create(schema, outputStream);
            schemaInitialized = true;
        }

        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);

        for (final RecordField field : record.fields()) {
            setFieldValue(builder, field);
        }

        final GenericRecord avroRecord = builder.build();
        writer.append(avroRecord);
    }

    private Schema inferSchema(
            final Record record) {

        final SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record("record").fields();

        for (final RecordField field : record.fields()) {
            fields.name(field.name()).type(toAvroSchema(field.type())).noDefault();
        }

        return fields.endRecord();
    }

    private Schema toAvroSchema(final RecordValueType type) {
        return switch (type) {
            case BOOLEAN -> Schema.create(Schema.Type.BOOLEAN);
            case INTEGER -> Schema.create(Schema.Type.LONG);
            case DECIMAL -> Schema.create(Schema.Type.DOUBLE);
            case STRING -> Schema.create(Schema.Type.STRING);
            case DATE, TIME, DATETIME -> Schema.create(Schema.Type.STRING);
            case BINARY -> Schema.create(Schema.Type.BYTES);
            case NULL -> Schema.create(Schema.Type.NULL);
            default -> Schema.create(Schema.Type.STRING);
        };
    }

    private void setFieldValue(
            final GenericRecordBuilder builder,
            final RecordField field)
            throws IOException {

        final Object value = field.value();
        final RecordValueType type = field.type();

        if (value == null || type == RecordValueType.NULL) {
            builder.set(field.name(), null);
            return;
        }

        switch (type) {
            case BOOLEAN:
                if (value instanceof Boolean) {
                    builder.set(field.name(), value);
                } else {
                    builder.set(field.name(), Boolean.parseBoolean(value.toString()));
                }
                break;

            case INTEGER:
                if (value instanceof Number) {
                    builder.set(field.name(), ((Number) value).longValue());
                } else {
                    builder.set(field.name(), Long.parseLong(value.toString()));
                }
                break;

            case DECIMAL:
                if (value instanceof Number) {
                    builder.set(field.name(), ((Number) value).doubleValue());
                } else {
                    builder.set(field.name(), Double.parseDouble(value.toString()));
                }
                break;

            case STRING:
            case DATE:
            case TIME:
            case DATETIME:
                builder.set(field.name(), value.toString());
                break;

            case BINARY:
                if (value instanceof byte[]) {
                    builder.set(field.name(), java.nio.ByteBuffer.wrap((byte[]) value));
                } else {
                    builder.set(field.name(), java.nio.ByteBuffer.wrap(value.toString().getBytes()));
                }
                break;

            default:
                builder.set(field.name(), value.toString());
        }
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (writer != null) {
            // If schema was never initialized (no records written), create empty file with minimal schema
            if (!schemaInitialized) {
                try {
                    final Schema emptySchema = SchemaBuilder.record("record").fields().endRecord();
                    writer.create(emptySchema, outputStream);
                } catch (final IOException exception) {
                    failure = exception;
                }
            }

            try {
                writer.flush();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }

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

        writer = null;
        schema = null;
        outputStream = null;
        opened = false;
        schemaInitialized = false;

        if (failure != null) {
            throw failure;
        }
    }
}
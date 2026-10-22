package io.github.avinashio.ozhuku.format.parquet;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatWriter;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.io.LocalOutputFile;
import org.apache.hadoop.conf.Configuration;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Writes format-neutral Ozhuku records as Parquet.
 *
 * <p>The writer requires a schema to write Parquet records. The schema is
 * inferred from the first record written. The writer uses Apache Parquet's
 * AvroParquetWriter to stream records to the output, so memory usage stays
 * bounded regardless of record count.
 */
public final class ParquetFormatWriter implements FormatWriter {

    private final ParquetWriterConfiguration configuration;

    private ParquetWriter<GenericRecord> writer;
    private Schema schema;
    private OutputStream outputStream;
    private Path tempFile;
    private boolean opened;
    private boolean schemaInitialized;

    public ParquetFormatWriter(
            final ParquetWriterConfiguration configuration) {

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
                    "Parquet writer is already open");
        }

        // Parquet writer requires a file, so we write to a temp file first
        tempFile = Files.createTempFile("ozhuku-parquet-", ".parquet");
        tempFile.toFile().deleteOnExit();

        final Configuration hadoopConf = new Configuration();
        hadoopConf.set("parquet.enable.summary-metadata", "false");

        // Set compression codec
        final String codec = configuration.compressionCodec().toLowerCase();
        switch (codec) {
            case "snappy":
                hadoopConf.set("parquet.compression", "SNAPPY");
                break;
            case "gzip":
                hadoopConf.set("parquet.compression", "GZIP");
                break;
            case "zstd":
                hadoopConf.set("parquet.compression", "ZSTD");
                break;
            case "lz4":
                hadoopConf.set("parquet.compression", "LZ4");
                break;
            case "uncompressed":
                hadoopConf.set("parquet.compression", "UNCOMPRESSED");
                break;
            default:
                hadoopConf.set("parquet.compression", "SNAPPY");
        }

        // Note: schema will be inferred from first record written
        // For now, we can't build the writer without a schema
        // We'll initialize it on first write
        this.outputStream = outputStream;
        opened = true;
        schemaInitialized = false;
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
                    "Parquet writer must be opened before writing");
        }

        // Initialize schema and writer from first record if not provided
        if (!schemaInitialized) {
            schema = inferSchema(record);
            final Configuration hadoopConf = new Configuration();
            hadoopConf.set("parquet.enable.summary-metadata", "false");
            final String codec = configuration.compressionCodec().toLowerCase();
            switch (codec) {
                case "snappy":
                    hadoopConf.set("parquet.compression", "SNAPPY");
                    break;
                case "gzip":
                    hadoopConf.set("parquet.compression", "GZIP");
                    break;
                case "zstd":
                    hadoopConf.set("parquet.compression", "ZSTD");
                    break;
                case "lz4":
                    hadoopConf.set("parquet.compression", "LZ4");
                    break;
                case "uncompressed":
                    hadoopConf.set("parquet.compression", "UNCOMPRESSED");
                    break;
                default:
                    hadoopConf.set("parquet.compression", "SNAPPY");
            }

            writer = AvroParquetWriter.<GenericRecord>builder(new LocalOutputFile(tempFile))
                    .withConf(hadoopConf)
                    .withSchema(schema)
                    .withDataModel(GenericData.get())
                    .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                    .build();
            schemaInitialized = true;
        }

        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);

        for (final RecordField field : record.fields()) {
            setFieldValue(builder, field);
        }

        final GenericRecord avroRecord = builder.build();
        writer.write(avroRecord);
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
            try {
                writer.close();
            } catch (final IOException exception) {
                failure = exception;
            }

            // Copy temp file to output stream
            if (failure == null && tempFile != null && Files.exists(tempFile)) {
                try {
                    Files.copy(tempFile, outputStream);
                } catch (final IOException exception) {
                    failure = exception;
                }
            }
        }

        // Clean up temp file
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
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
        tempFile = null;
        opened = false;
        schemaInitialized = false;

        if (failure != null) {
            throw failure;
        }
    }
}
package io.github.avinashio.ozhuku.format.parquet;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.LocalInputFile;
import org.apache.parquet.schema.MessageType;
import org.apache.hadoop.conf.Configuration;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads Parquet input as format-neutral Ozhuku records.
 *
 * <p>The reader uses Apache Parquet's AvroParquetReader to stream records from
 * a Parquet file. The Parquet file's embedded schema (stored as Avro schema)
 * is used to decode the records. Each Parquet record becomes a single Ozhuku
 * record, with each field in the record becoming a named field in the Ozhuku record.
 *
 * <p>The reader is streaming so memory usage stays bounded regardless of
 * input size.
 */
public final class ParquetFormatReader implements FormatReader {

    private final ParquetReaderConfiguration configuration;

    private ParquetReader<GenericRecord> reader;
    private Schema schema;
    private long sequence;
    private boolean opened;
    private boolean endOfInput;
    private Path tempFile;

    public ParquetFormatReader(
            final ParquetReaderConfiguration configuration) {

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
                    "Parquet reader is already open");
        }

        // Parquet reader requires a file, so we need to write to a temp file first
        // This is a limitation of the Parquet API which works with file system abstraction
        tempFile = Files.createTempFile("ozhuku-parquet-", ".parquet");
        tempFile.toFile().deleteOnExit();
        Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        final Configuration hadoopConf = new Configuration();
        // Disable checksums for better performance on temp files
        hadoopConf.set("parquet.enable.summary-metadata", "false");

        // Read the schema from the Parquet file first using LocalInputFile to avoid Hadoop FS
        final LocalInputFile localInputFile = new LocalInputFile(tempFile);
        final ParquetFileReader fileReader = ParquetFileReader.open(localInputFile);
        final MessageType parquetSchema = fileReader.getFileMetaData().getSchema();
        fileReader.close();

        // Convert Parquet schema to Avro schema
        final AvroSchemaConverter converter = new AvroSchemaConverter(hadoopConf);
        schema = converter.convert(parquetSchema);

        reader = AvroParquetReader.<GenericRecord>builder(new LocalInputFile(tempFile))
                .withConf(hadoopConf)
                .build();

        sequence = 0;
        endOfInput = false;
        opened = true;
    }

    @Override
    public FormatReadResult read()
            throws IOException {

        if (!opened) {
            throw new IllegalStateException(
                    "Parquet reader must be opened before reading");
        }

        if (endOfInput) {
            return FormatReadResult.endOfInput();
        }

        final GenericRecord avroRecord = reader.read();

        if (avroRecord == null) {
            endOfInput = true;
            return FormatReadResult.endOfInput();
        }

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

        reader = null;
        schema = null;
        tempFile = null;
        opened = false;
        endOfInput = false;
        sequence = 0;

        if (failure != null) {
            throw failure;
        }
    }
}
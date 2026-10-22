package io.github.avinashio.ozhuku.format.parquet;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.io.LocalOutputFile;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParquetFormatReaderTest {

    private void writeTestParquetFile(File testFile, Schema schema, List<GenericRecord> records) throws IOException {
        final Configuration hadoopConf = new Configuration();
        final org.apache.parquet.hadoop.ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(
                        new LocalOutputFile(testFile.toPath()))
                .withConf(hadoopConf)
                .withSchema(schema)
                .withDataModel(GenericData.get())
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();

        for (GenericRecord record : records) {
            writer.write(record);
        }
        writer.close();
    }

    @Test
    void shouldReadParquetRecords(@TempDir File tempDir) throws Exception {

        // Create test Parquet data
        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"id\",\"type\":\"long\"},"
                + "{\"name\":\"name\",\"type\":\"string\"}]}");

        final File testFile = new File(tempDir, "test.parquet");
        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);

        builder.set("id", 1L);
        builder.set("name", "Avinash");
        final GenericRecord record1 = builder.build();

        builder.set("id", 2L);
        builder.set("name", "John");
        final GenericRecord record2 = builder.build();

        writeTestParquetFile(testFile, schema, List.of(record1, record2));

        // Read it back
        final byte[] parquetData = Files.readAllBytes(testFile.toPath());

        final ParquetFormatReader reader =
                new ParquetFormatReader(ParquetReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(parquetData));

        final FormatReadResult first = reader.read();

        assertEquals(FormatReadResult.Status.RECORD, first.status());

        final Record firstRecord = first.record();

        assertEquals(0, firstRecord.sequence());
        assertEquals(2, firstRecord.fields().size());

        assertEquals("id", firstRecord.fields().get(0).name());
        assertEquals(1L, firstRecord.fields().get(0).value());
        assertEquals(RecordValueType.INTEGER, firstRecord.fields().get(0).type());

        assertEquals("name", firstRecord.fields().get(1).name());
        assertEquals("Avinash", firstRecord.fields().get(1).value());
        assertEquals(RecordValueType.STRING, firstRecord.fields().get(1).type());

        final FormatReadResult second = reader.read();

        assertEquals(FormatReadResult.Status.RECORD, second.status());
        assertEquals(1, second.record().sequence());

        assertEquals("John", second.record().fields().get(1).value());

        final FormatReadResult end = reader.read();

        assertEquals(FormatReadResult.Status.END_OF_INPUT, end.status());

        reader.close();
    }

    @Test
    void shouldReadBooleanAndNullValues(@TempDir File tempDir) throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"active\",\"type\":[\"null\",\"boolean\"]},"
                + "{\"name\":\"deleted\",\"type\":[\"null\",\"boolean\"]}]}");

        final File testFile = new File(tempDir, "test.parquet");
        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);
        builder.set("active", true);
        builder.set("deleted", null);
        final GenericRecord avroRecord = builder.build();
        writeTestParquetFile(testFile, schema, List.of(avroRecord));

        final byte[] parquetData = Files.readAllBytes(testFile.toPath());

        final ParquetFormatReader reader =
                new ParquetFormatReader(ParquetReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(parquetData));

        final FormatReadResult result = reader.read();

        assertEquals(FormatReadResult.Status.RECORD, result.status());

        final Record record = result.record();

        assertEquals("active", record.fields().get(0).name());
        assertEquals(true, record.fields().get(0).value());
        assertEquals(RecordValueType.BOOLEAN, record.fields().get(0).type());

        assertEquals("deleted", record.fields().get(1).name());
        assertEquals(null, record.fields().get(1).value());
        assertEquals(RecordValueType.NULL, record.fields().get(1).type());

        reader.close();
    }

    @Test
    void shouldReadNumericValues(@TempDir File tempDir) throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"count\",\"type\":\"long\"},"
                + "{\"name\":\"price\",\"type\":\"double\"}]}");

        final File testFile = new File(tempDir, "test.parquet");
        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);
        builder.set("count", 42L);
        builder.set("price", 19.95);
        final GenericRecord avroRecord = builder.build();
        writeTestParquetFile(testFile, schema, List.of(avroRecord));

        final byte[] parquetData = Files.readAllBytes(testFile.toPath());

        final ParquetFormatReader reader =
                new ParquetFormatReader(ParquetReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(parquetData));

        final FormatReadResult result = reader.read();

        assertEquals(FormatReadResult.Status.RECORD, result.status());

        final Record record = result.record();

        assertEquals(42L, record.fields().get(0).value());
        assertEquals(RecordValueType.INTEGER, record.fields().get(0).type());

        assertEquals(19.95, record.fields().get(1).value());
        assertEquals(RecordValueType.DECIMAL, record.fields().get(1).type());

        reader.close();
    }

    @Test
    void shouldHandleEmptyInput(@TempDir File tempDir) throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"id\",\"type\":\"long\"}]}");

        final File testFile = new File(tempDir, "test.parquet");
        final Configuration hadoopConf = new Configuration();
        final org.apache.parquet.hadoop.ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(
                        new LocalOutputFile(testFile.toPath()))
                .withConf(hadoopConf)
                .withSchema(schema)
                .withDataModel(GenericData.get())
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();

        writer.close();

        final byte[] parquetData = Files.readAllBytes(testFile.toPath());

        final ParquetFormatReader reader =
                new ParquetFormatReader(ParquetReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(parquetData));

        final FormatReadResult result = reader.read();

        assertEquals(FormatReadResult.Status.END_OF_INPUT, result.status());

        reader.close();
    }

    @Test
    void shouldRejectReadBeforeOpen() {

        final ParquetFormatReader reader =
                new ParquetFormatReader(ParquetReaderConfiguration.defaults());

        assertThrows(IllegalStateException.class, reader::read);
    }

    @Test
    void shouldRejectNullInputStream() {

        final ParquetFormatReader reader =
                new ParquetFormatReader(ParquetReaderConfiguration.defaults());

        assertThrows(NullPointerException.class, () -> reader.open(null));
    }

    @Test
    void shouldRejectOpeningReaderTwice(@TempDir File tempDir) throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"id\",\"type\":\"long\"}]}");

        final File testFile = new File(tempDir, "test.parquet");
        final Configuration hadoopConf = new Configuration();
        final org.apache.parquet.hadoop.ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(
                        new LocalOutputFile(testFile.toPath()))
                .withConf(hadoopConf)
                .withSchema(schema)
                .withDataModel(GenericData.get())
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();

        writer.close();

        final byte[] parquetData = Files.readAllBytes(testFile.toPath());

        final ParquetFormatReader reader =
                new ParquetFormatReader(ParquetReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(parquetData));

        assertThrows(
                IllegalStateException.class,
                () -> reader.open(new ByteArrayInputStream(parquetData)));

        reader.close();
    }
}
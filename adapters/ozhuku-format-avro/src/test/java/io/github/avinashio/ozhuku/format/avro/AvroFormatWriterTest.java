package io.github.avinashio.ozhuku.format.avro;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvroFormatWriterTest {

    @Test
    void shouldWriteAvroRecords() throws Exception {

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(output);

        final Record record = new Record(
                0,
                List.of(
                        new RecordField("id", RecordValueType.INTEGER, 1L),
                        new RecordField("name", RecordValueType.STRING, "Avinash")),
                RecordMetadata.empty());

        writer.write(record);
        writer.close();

        // Read back and verify
        final byte[] avroData = output.toByteArray();
        final DataFileStream<GenericRecord> reader =
                new DataFileStream<>(
                        new ByteArrayInputStream(avroData),
                        new GenericDatumReader<>());

        final Schema schema = reader.getSchema();
        assertEquals("record", schema.getName());

        final GenericRecord first = reader.next();
        assertEquals(1L, first.get("id"));
        assertEquals("Avinash", first.get("name").toString());

        assertEquals(false, reader.hasNext());
        reader.close();
    }

    @Test
    void shouldWriteMultipleRecords() throws Exception {

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(output);

        writer.write(new Record(
                0,
                List.of(new RecordField("id", RecordValueType.INTEGER, 1L)),
                RecordMetadata.empty()));

        writer.write(new Record(
                1,
                List.of(new RecordField("id", RecordValueType.INTEGER, 2L)),
                RecordMetadata.empty()));

        writer.close();

        final byte[] avroData = output.toByteArray();
        final DataFileStream<GenericRecord> reader =
                new DataFileStream<>(
                        new ByteArrayInputStream(avroData),
                        new GenericDatumReader<>());

        final GenericRecord first = reader.next();
        assertEquals(1L, first.get("id"));

        final GenericRecord second = reader.next();
        assertEquals(2L, second.get("id"));

        assertEquals(false, reader.hasNext());
        reader.close();
    }

    @Test
    void shouldWriteWithProvidedSchema() throws Exception {

        final String schemaJson =
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"id\",\"type\":\"long\"},"
                + "{\"name\":\"name\",\"type\":\"string\"}]}";

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final AvroWriterConfiguration config =
                AvroWriterConfiguration.builder()
                        .schemaJson(schemaJson)
                        .build();

        final AvroFormatWriter writer = new AvroFormatWriter(config);

        writer.open(output);

        final Record record = new Record(
                0,
                List.of(
                        new RecordField("id", RecordValueType.INTEGER, 42L),
                        new RecordField("name", RecordValueType.STRING, "Test")),
                RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final byte[] avroData = output.toByteArray();
        final DataFileStream<GenericRecord> reader =
                new DataFileStream<>(
                        new ByteArrayInputStream(avroData),
                        new GenericDatumReader<>());

        final Schema schema = reader.getSchema();
        assertEquals("TestRecord", schema.getName());
        assertEquals(2, schema.getFields().size());

        final GenericRecord avroRecord = reader.next();
        assertEquals(42L, avroRecord.get("id"));
        assertEquals("Test", avroRecord.get("name").toString());

        reader.close();
    }

    @Test
    void shouldWriteNumericValues() throws Exception {

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(output);

        final Record record = new Record(
                0,
                List.of(
                        new RecordField("count", RecordValueType.INTEGER, 42L),
                        new RecordField("price", RecordValueType.DECIMAL, 19.95)),
                RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final byte[] avroData = output.toByteArray();
        final DataFileStream<GenericRecord> reader =
                new DataFileStream<>(
                        new ByteArrayInputStream(avroData),
                        new GenericDatumReader<>());

        final GenericRecord avroRecord = reader.next();
        assertEquals(42L, avroRecord.get("count"));
        assertEquals(19.95, avroRecord.get("price"));

        reader.close();
    }

    @Test
    void shouldWriteBooleanAndNullValues() throws Exception {

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(output);

        final Record record = new Record(
                0,
                List.of(
                        new RecordField("active", RecordValueType.BOOLEAN, true),
                        new RecordField("deleted", RecordValueType.NULL, null)),
                RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final byte[] avroData = output.toByteArray();
        final DataFileStream<GenericRecord> reader =
                new DataFileStream<>(
                        new ByteArrayInputStream(avroData),
                        new GenericDatumReader<>());

        final GenericRecord avroRecord = reader.next();
        assertEquals(true, avroRecord.get("active"));
        assertEquals(null, avroRecord.get("deleted"));

        reader.close();
    }

    @Test
    void shouldRejectWriteBeforeOpen() {

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        final Record record = new Record(
                0,
                List.of(),
                RecordMetadata.empty());

        assertThrows(IllegalStateException.class, () -> writer.write(record));
    }

    @Test
    void shouldRejectNullRecordAfterOpen() throws Exception {

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(new ByteArrayOutputStream());

        assertThrows(NullPointerException.class, () -> writer.write(null));

        writer.close();
    }

    @Test
    void shouldRejectOpeningWriterTwice() throws Exception {

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(new ByteArrayOutputStream());

        assertThrows(
                IllegalStateException.class,
                () -> writer.open(new ByteArrayOutputStream()));

        writer.close();
    }

    @Test
    void shouldWriteBinaryData() throws Exception {

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(output);

        final byte[] binaryData = new byte[]{0, 1, 2, 3, 4, 5};
        final Record record = new Record(
                0,
                List.of(
                        new RecordField("data", RecordValueType.BINARY, binaryData)),
                RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final byte[] avroData = output.toByteArray();
        final DataFileStream<GenericRecord> reader =
                new DataFileStream<>(
                        new ByteArrayInputStream(avroData),
                        new GenericDatumReader<>());

        final GenericRecord avroRecord = reader.next();
        final Object data = avroRecord.get("data");

        // Avro returns ByteBuffer for BYTES type
        final byte[] readData;
        if (data instanceof java.nio.ByteBuffer) {
            final java.nio.ByteBuffer buffer = (java.nio.ByteBuffer) data;
            readData = new byte[buffer.remaining()];
            buffer.get(readData);
        } else if (data instanceof byte[]) {
            readData = (byte[]) data;
        } else {
            throw new AssertionError("Expected ByteBuffer or byte[], got " + data.getClass());
        }

        assertEquals(binaryData.length, readData.length);
        for (int i = 0; i < binaryData.length; i++) {
            assertEquals(binaryData[i], readData[i]);
        }

        reader.close();
    }

    @Test
    void shouldWriteDateTimeAsString() throws Exception {

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final AvroFormatWriter writer =
                new AvroFormatWriter(AvroWriterConfiguration.defaults());

        writer.open(output);

        final Record record = new Record(
                0,
                List.of(
                        new RecordField("created", RecordValueType.DATETIME, "2024-01-15T10:30:00Z")),
                RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final byte[] avroData = output.toByteArray();
        final DataFileStream<GenericRecord> reader =
                new DataFileStream<>(
                        new ByteArrayInputStream(avroData),
                        new GenericDatumReader<>());

        final GenericRecord avroRecord = reader.next();
        assertEquals("2024-01-15T10:30:00Z", avroRecord.get("created").toString());

        reader.close();
    }
}
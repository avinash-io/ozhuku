package io.github.avinashio.ozhuku.format.avro;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvroFormatReaderTest {

    @Test
    void shouldReadAvroRecords() throws Exception {

        // Create test Avro data
        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"id\",\"type\":\"long\"},"
                + "{\"name\":\"name\",\"type\":\"string\"}]}");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final DataFileWriter<GenericRecord> writer =
                new DataFileWriter<>(new GenericDatumWriter<>(schema));
        writer.create(schema, output);

        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);
        builder.set("id", 1L);
        builder.set("name", "Avinash");
        writer.append(builder.build());

        builder.set("id", 2L);
        builder.set("name", "John");
        writer.append(builder.build());

        writer.close();

        final byte[] avroData = output.toByteArray();

        // Read it back
        final AvroFormatReader reader =
                new AvroFormatReader(AvroReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(avroData));

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
    void shouldReadBooleanAndNullValues() throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"active\",\"type\":[\"null\",\"boolean\"]},"
                + "{\"name\":\"deleted\",\"type\":[\"null\",\"boolean\"]}]}");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final DataFileWriter<GenericRecord> writer =
                new DataFileWriter<>(new GenericDatumWriter<>(schema));
        writer.create(schema, output);

        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);
        builder.set("active", true);
        builder.set("deleted", null);
        writer.append(builder.build());

        writer.close();

        final byte[] avroData = output.toByteArray();

        final AvroFormatReader reader =
                new AvroFormatReader(AvroReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(avroData));

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
    void shouldReadNumericValues() throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"count\",\"type\":\"long\"},"
                + "{\"name\":\"price\",\"type\":\"double\"}]}");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final DataFileWriter<GenericRecord> writer =
                new DataFileWriter<>(new GenericDatumWriter<>(schema));
        writer.create(schema, output);

        final GenericRecordBuilder builder = new GenericRecordBuilder(schema);
        builder.set("count", 42L);
        builder.set("price", 19.95);
        writer.append(builder.build());

        writer.close();

        final byte[] avroData = output.toByteArray();

        final AvroFormatReader reader =
                new AvroFormatReader(AvroReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(avroData));

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
    void shouldHandleEmptyInput() throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"id\",\"type\":\"long\"}]}");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final DataFileWriter<GenericRecord> writer =
                new DataFileWriter<>(new GenericDatumWriter<>(schema));
        writer.create(schema, output);
        writer.close();

        final byte[] avroData = output.toByteArray();

        final AvroFormatReader reader =
                new AvroFormatReader(AvroReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(avroData));

        final FormatReadResult result = reader.read();

        assertEquals(FormatReadResult.Status.END_OF_INPUT, result.status());

        reader.close();
    }

    @Test
    void shouldRejectReadBeforeOpen() {

        final AvroFormatReader reader =
                new AvroFormatReader(AvroReaderConfiguration.defaults());

        assertThrows(IllegalStateException.class, reader::read);
    }

    @Test
    void shouldRejectNullInputStream() {

        final AvroFormatReader reader =
                new AvroFormatReader(AvroReaderConfiguration.defaults());

        assertThrows(NullPointerException.class, () -> reader.open(null));
    }

    @Test
    void shouldRejectOpeningReaderTwice() throws Exception {

        final Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":["
                + "{\"name\":\"id\",\"type\":\"long\"}]}");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final DataFileWriter<GenericRecord> writer =
                new DataFileWriter<>(new GenericDatumWriter<>(schema));
        writer.create(schema, output);
        writer.close();

        final byte[] avroData = output.toByteArray();

        final AvroFormatReader reader =
                new AvroFormatReader(AvroReaderConfiguration.defaults());

        reader.open(new ByteArrayInputStream(avroData));

        assertThrows(
                IllegalStateException.class,
                () -> reader.open(new ByteArrayInputStream(avroData)));

        reader.close();
    }
}
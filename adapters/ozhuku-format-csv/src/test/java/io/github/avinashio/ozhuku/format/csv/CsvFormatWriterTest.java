package io.github.avinashio.ozhuku.format.csv;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvFormatWriterTest {

    @Test
    void shouldWriteCsvRecord() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.defaults());

        writer.open(output);

        final Record record =
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "id",
                                        RecordValueType.STRING,
                                        "1"),
                                new RecordField(
                                        "name",
                                        RecordValueType.STRING,
                                        "Avinash")),
                        RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                "id,name\r\n"
                        + "1,Avinash\r\n",
                result);
    }

    @Test
    void shouldWriteHeaderOnlyOnce() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.defaults());

        writer.open(output);

        writer.write(
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "id",
                                        RecordValueType.STRING,
                                        "1")),
                        RecordMetadata.empty()));

        writer.write(
                new Record(
                        1,
                        List.of(
                                new RecordField(
                                        "id",
                                        RecordValueType.STRING,
                                        "2")),
                        RecordMetadata.empty()));

        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                "id\r\n"
                        + "1\r\n"
                        + "2\r\n",
                result);
    }

    @Test
    void shouldWriteWithoutHeader() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final CsvWriterConfiguration configuration =
                CsvWriterConfiguration.builder()
                        .writeHeader(false)
                        .build();

        final CsvFormatWriter writer =
                new CsvFormatWriter(configuration);

        writer.open(output);

        writer.write(
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "id",
                                        RecordValueType.STRING,
                                        "1")),
                        RecordMetadata.empty()));

        writer.close();

        assertEquals(
                "1\r\n",
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void shouldQuoteDelimiterInsideValue()
            throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.defaults());

        writer.open(output);

        writer.write(
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "name",
                                        RecordValueType.STRING,
                                        "Avinash, Menon")),
                        RecordMetadata.empty()));

        writer.close();

        assertEquals(
                "name\r\n"
                        + "\"Avinash, Menon\"\r\n",
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void shouldRejectWriteBeforeOpen() {

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.defaults());

        final Record record =
                new Record(
                        0,
                        List.of(),
                        RecordMetadata.empty());

        assertThrows(
                IllegalStateException.class,
                () -> writer.write(record));
    }

    @Test
    void shouldRejectNullRecordAfterOpen()
            throws Exception {

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.defaults());

        writer.open(
                new ByteArrayOutputStream());

        assertThrows(
                NullPointerException.class,
                () -> writer.write(null));

        writer.close();
    }
}
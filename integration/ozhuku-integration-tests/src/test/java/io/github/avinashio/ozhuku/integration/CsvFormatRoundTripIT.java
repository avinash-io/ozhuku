package io.github.avinashio.ozhuku.integration;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.csv.CsvFormatReader;
import io.github.avinashio.ozhuku.format.csv.CsvFormatWriter;
import io.github.avinashio.ozhuku.format.csv.CsvReaderConfiguration;
import io.github.avinashio.ozhuku.format.csv.CsvWriterConfiguration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvFormatRoundTripIT {

    @Test
    void shouldRoundTripCsvThroughFormatAdapters()
            throws Exception {

        final String input =
                "id,name,city\r\n"
                        + "1,Avinash,Thrissur\r\n"
                        + "2,John,London\r\n";

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        input.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult first =
                reader.read();

        final FormatReadResult second =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                first.status());

        assertEquals(
                FormatReadResult.Status.RECORD,
                second.status());

        final Record firstRecord =
                first.record();

        final Record secondRecord =
                second.record();

        assertEquals(
                "1",
                firstRecord.fields().get(0).value());

        assertEquals(
                "Avinash",
                firstRecord.fields().get(1).value());

        assertEquals(
                "Thrissur",
                firstRecord.fields().get(2).value());

        assertEquals(
                "2",
                secondRecord.fields().get(0).value());

        reader.close();

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.defaults());

        writer.open(output);

        writer.write(firstRecord);
        writer.write(secondRecord);

        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                input,
                result);
    }

    @Test
    void shouldPreserveQuotedValuesDuringRoundTrip()
            throws Exception {

        final String input =
                "id,name\r\n"
                        + "1,\"Avinash, Menon\"\r\n";

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        input.getBytes(StandardCharsets.UTF_8)));

        final Record record =
                reader.read().record();

        reader.close();

        assertEquals(
                "Avinash, Menon",
                record.fields()
                        .get(1)
                        .value());

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.defaults());

        writer.open(output);
        writer.write(record);
        writer.close();

        assertEquals(
                input,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void shouldRoundTripCustomDelimiter()
            throws Exception {

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

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.builder()
                                .delimiter(';')
                                .build());

        writer.open(output);
        writer.write(record);
        writer.close();

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.builder()
                                .delimiter(';')
                                .build());

        reader.open(
                new ByteArrayInputStream(
                        output.toByteArray()));

        final Record result =
                reader.read().record();

        reader.close();

        assertEquals(
                "1",
                result.fields().get(0).value());

        assertEquals(
                "Avinash",
                result.fields().get(1).value());
    }
}
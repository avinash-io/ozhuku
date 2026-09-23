package io.github.avinashio.ozhuku.format.csv;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvFormatReaderTest {

    @Test
    void shouldReadCsvRecords() throws Exception {

        final String csv =
                "id,name\n"
                        + "1,Avinash\n"
                        + "2,John\n";

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        csv.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult first =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                first.status());

        final Record firstRecord =
                first.record();

        assertEquals(0, firstRecord.sequence());
        assertEquals(2, firstRecord.fields().size());

        assertEquals(
                "id",
                firstRecord.fields().get(0).name());

        assertEquals(
                "1",
                firstRecord.fields().get(0).value());

        assertEquals(
                "name",
                firstRecord.fields().get(1).name());

        assertEquals(
                "Avinash",
                firstRecord.fields().get(1).value());

        final FormatReadResult second =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                second.status());

        assertEquals(
                1,
                second.record().sequence());

        final FormatReadResult end =
                reader.read();

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                end.status());

        reader.close();
    }

    @Test
    void shouldReadCsvWithoutHeader() throws Exception {

        final String csv =
                "1,Avinash\n"
                        + "2,John\n";

        final CsvReaderConfiguration configuration =
                CsvReaderConfiguration.builder()
                        .headerPresent(false)
                        .build();

        final CsvFormatReader reader =
                new CsvFormatReader(configuration);

        reader.open(
                new ByteArrayInputStream(
                        csv.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                result.status());

        assertEquals(
                "field0",
                result.record().fields().get(0).name());

        assertEquals(
                "field1",
                result.record().fields().get(1).name());

        reader.close();
    }

    @Test
    void shouldSupportCustomDelimiter() throws Exception {

        final String csv =
                "id;name\n"
                        + "1;Avinash\n";

        final CsvReaderConfiguration configuration =
                CsvReaderConfiguration.builder()
                        .delimiter(';')
                        .build();

        final CsvFormatReader reader =
                new CsvFormatReader(configuration);

        reader.open(
                new ByteArrayInputStream(
                        csv.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                "Avinash",
                result.record()
                        .fields()
                        .get(1)
                        .value());

        reader.close();
    }

    @Test
    void shouldRejectReadBeforeOpen() {

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        assertThrows(
                IllegalStateException.class,
                reader::read);
    }

    @Test
    void shouldRejectOpeningReaderTwice()
            throws Exception {

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        final ByteArrayInputStream input =
                new ByteArrayInputStream(
                        "id\n1\n"
                                .getBytes(StandardCharsets.UTF_8));

        reader.open(input);

        assertThrows(
                IllegalStateException.class,
                () -> reader.open(
                        new ByteArrayInputStream(
                                "id\n2\n"
                                        .getBytes(
                                                StandardCharsets.UTF_8))));

        reader.close();
    }

    @Test
    void shouldHandleEmptyInput() throws Exception {

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        new byte[0]));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                result.status());

        reader.close();
    }

    @Test
    void shouldHandleHeaderOnlyInput() throws Exception {

        final String csv =
                "id,name\n";

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        csv.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                result.status());

        reader.close();
    }

    @Test
    void shouldHandleQuotedDelimiter() throws Exception {

        final String csv =
                "id,name\n"
                        + "1,\"Avinash, Menon\"\n";

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        csv.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                result.status());

        assertEquals(
                "Avinash, Menon",
                result.record()
                        .fields()
                        .get(1)
                        .value());

        reader.close();
    }

    @Test
    void shouldIncrementRecordSequence() throws Exception {

        final String csv =
                "id\n"
                        + "1\n"
                        + "2\n"
                        + "3\n";

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(
                0,
                reader.read().record().sequence());

        assertEquals(
                1,
                reader.read().record().sequence());

        assertEquals(
                2,
                reader.read().record().sequence());

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                reader.read().status());

        reader.close();
    }

    @Test
    void shouldRejectNullInputStream() {

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        assertThrows(
                NullPointerException.class,
                () -> reader.open(null));
    }

    @Test
    void shouldAllowReadAfterEndOfInput() throws Exception {

        final String csv =
                "id\n"
                        + "1\n";

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(
                FormatReadResult.Status.RECORD,
                reader.read().status());

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                reader.read().status());

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                reader.read().status());

        reader.close();
    }

}

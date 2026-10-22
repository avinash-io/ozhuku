package io.github.avinashio.ozhuku.format.json;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonFormatReaderTest {

    @Test
    void shouldReadJsonArrayObjects() throws Exception {

        final String json =
                "[{\"id\":\"1\",\"name\":\"Avinash\"},"
                + "{\"id\":\"2\",\"name\":\"John\"}]";

        final JsonFormatReader reader =
                new JsonFormatReader(
                        JsonReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        json.getBytes(StandardCharsets.UTF_8)));

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
    void shouldReadNewlineDelimitedJson() throws Exception {

        final String json =
                "{\"id\":\"1\",\"name\":\"Avinash\"}\n"
                + "{\"id\":\"2\",\"name\":\"John\"}";

        final JsonReaderConfiguration configuration =
                JsonReaderConfiguration.builder()
                        .build();

        final JsonFormatReader reader =
                new JsonFormatReader(configuration);

        reader.open(
                new ByteArrayInputStream(
                        json.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult first =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                first.status());

        assertEquals(
                0,
                first.record().sequence());

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
    void shouldReadBooleanAndNullValues() throws Exception {

        final String json =
                "{\"active\":true,\"deleted\":null}";

        final JsonFormatReader reader =
                new JsonFormatReader(
                        JsonReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        json.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                result.status());

        final Record record =
                result.record();

        assertEquals(
                "active",
                record.fields().get(0).name());
        assertEquals(
                true,
                record.fields().get(0).value());

        assertEquals(
                "deleted",
                record.fields().get(1).name());
        assertEquals(
                null,
                record.fields().get(1).value());

        reader.close();
    }

    @Test
    void shouldReadNumericValues() throws Exception {

        final String json =
                "{\"count\":42,\"price\":19.95}";

        final JsonFormatReader reader =
                new JsonFormatReader(
                        JsonReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        json.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                result.status());

        final Record record =
                result.record();

        assertEquals(
                42L,
                record.fields().get(0).value());

        assertEquals(
                19.95,
                record.fields().get(1).value());

        reader.close();
    }

    @Test
    void shouldHandleEmptyInput() throws Exception {

        final JsonFormatReader reader =
                new JsonFormatReader(
                        JsonReaderConfiguration.defaults());

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
    void shouldRejectReadBeforeOpen() {

        final JsonFormatReader reader =
                new JsonFormatReader(
                        JsonReaderConfiguration.defaults());

        assertThrows(
                IllegalStateException.class,
                reader::read);
    }

    @Test
    void shouldRejectNullInputStream() {

        final JsonFormatReader reader =
                new JsonFormatReader(
                        JsonReaderConfiguration.defaults());

        assertThrows(
                NullPointerException.class,
                () -> reader.open(null));
    }

    @Test
    void shouldRejectOpeningReaderTwice()
            throws Exception {

        final JsonFormatReader reader =
                new JsonFormatReader(
                        JsonReaderConfiguration.defaults());

        final ByteArrayInputStream input =
                new ByteArrayInputStream(
                        "{\"id\":\"1\"}"
                                .getBytes(StandardCharsets.UTF_8));

        reader.open(input);

        assertThrows(
                IllegalStateException.class,
                () -> reader.open(
                        new ByteArrayInputStream(
                                "{\"id\":\"2\"}"
                                        .getBytes(
                                                StandardCharsets.UTF_8))));

        reader.close();
    }
}

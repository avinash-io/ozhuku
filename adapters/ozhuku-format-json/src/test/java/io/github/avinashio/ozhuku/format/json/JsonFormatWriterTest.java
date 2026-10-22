package io.github.avinashio.ozhuku.format.json;

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

class JsonFormatWriterTest {

    @Test
    void shouldWriteJsonArrayObjects() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final JsonFormatWriter writer =
                new JsonFormatWriter(
                        JsonWriterConfiguration.defaults());

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
                "[{\"id\":\"1\",\"name\":\"Avinash\"}]",
                result);
    }

    @Test
    void shouldWriteMultipleRecords() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final JsonFormatWriter writer =
                new JsonFormatWriter(
                        JsonWriterConfiguration.defaults());

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
                "[{\"id\":\"1\"},{\"id\":\"2\"}]",
                result);
    }

    @Test
    void shouldWriteWithoutArrayWrapper() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final JsonWriterConfiguration configuration =
                JsonWriterConfiguration.builder()
                        .arrayWrapped(false)
                        .build();

        final JsonFormatWriter writer =
                new JsonFormatWriter(configuration);

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

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                "{\"id\":\"1\"}",
                result);
    }

    @Test
    void shouldWriteNumericValues() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final JsonFormatWriter writer =
                new JsonFormatWriter(
                        JsonWriterConfiguration.defaults());

        writer.open(output);

        final Record record =
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "count",
                                        RecordValueType.INTEGER,
                                        42L),
                                new RecordField(
                                        "price",
                                        RecordValueType.DECIMAL,
                                        19.95)),
                        RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                "[{\"count\":42,\"price\":19.95}]",
                result);
    }

    @Test
    void shouldWriteBooleanAndNullValues() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final JsonFormatWriter writer =
                new JsonFormatWriter(
                        JsonWriterConfiguration.defaults());

        writer.open(output);

        final Record record =
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "active",
                                        RecordValueType.BOOLEAN,
                                        true),
                                new RecordField(
                                        "deleted",
                                        RecordValueType.NULL,
                                        null)),
                        RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                "[{\"active\":true,\"deleted\":null}]",
                result);
    }

    @Test
    void shouldRejectWriteBeforeOpen() {

        final JsonFormatWriter writer =
                new JsonFormatWriter(
                        JsonWriterConfiguration.defaults());

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

        final JsonFormatWriter writer =
                new JsonFormatWriter(
                        JsonWriterConfiguration.defaults());

        writer.open(
                new ByteArrayOutputStream());

        assertThrows(
                NullPointerException.class,
                () -> writer.write(null));

        writer.close();
    }

    @Test
    void shouldRejectOpeningWriterTwice()
            throws Exception {

        final JsonFormatWriter writer =
                new JsonFormatWriter(
                        JsonWriterConfiguration.defaults());

        writer.open(
                new ByteArrayOutputStream());

        assertThrows(
                IllegalStateException.class,
                () -> writer.open(
                        new ByteArrayOutputStream()));

        writer.close();
    }
}

package io.github.avinashio.ozhuku.format.xml;

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

class XmlFormatWriterTest {

    private static final String XML_DECLARATION =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @Test
    void shouldWriteRecords() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

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
                XML_DECLARATION
                + "<records><record><id>1</id><name>Avinash</name></record></records>",
                result);
    }

    @Test
    void shouldWriteMultipleRecords() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

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
                XML_DECLARATION
                + "<records>"
                + "<record><id>1</id></record>"
                + "<record><id>2</id></record>"
                + "</records>",
                result);
    }

    @Test
    void shouldWriteNumericValues() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

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
                XML_DECLARATION
                + "<records><record><count>42</count><price>19.95</price></record></records>",
                result);
    }

    @Test
    void shouldWriteBooleanAndNullValues() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

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
                XML_DECLARATION
                + "<records>"
                + "<record><active>true</active><deleted nil=\"true\"/></record>"
                + "</records>",
                result);
    }

    @Test
    void shouldRejectWriteBeforeOpen() {

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

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

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

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

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

        writer.open(
                new ByteArrayOutputStream());

        assertThrows(
                IllegalStateException.class,
                () -> writer.open(
                        new ByteArrayOutputStream()));

        writer.close();
    }

    @Test
    void shouldUseCustomElements() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final XmlWriterConfiguration configuration =
                XmlWriterConfiguration.builder()
                        .rootElement("rows")
                        .recordElement("row")
                        .build();

        final XmlFormatWriter writer =
                new XmlFormatWriter(configuration);

        writer.open(output);

        final Record record =
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "id",
                                        RecordValueType.STRING,
                                        "1")),
                        RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                XML_DECLARATION
                + "<rows><row><id>1</id></row></rows>",
                result);
    }

    @Test
    void shouldWriteEmptyRecord() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

        writer.open(output);

        final Record record =
                new Record(
                        0,
                        List.of(),
                        RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                XML_DECLARATION
                + "<records><record/></records>",
                result);
    }

    @Test
    void shouldWriteBinaryAsBase64() throws Exception {

        final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        final XmlFormatWriter writer =
                new XmlFormatWriter(
                        XmlWriterConfiguration.defaults());

        writer.open(output);

        final Record record =
                new Record(
                        0,
                        List.of(
                                new RecordField(
                                        "data",
                                        RecordValueType.BINARY,
                                        new byte[]{0, 1, 2, 3})),
                        RecordMetadata.empty());

        writer.write(record);
        writer.close();

        final String result =
                output.toString(StandardCharsets.UTF_8);

        assertEquals(
                XML_DECLARATION
                + "<records><record><data>AAECAw==</data></record></records>",
                result);
    }
}

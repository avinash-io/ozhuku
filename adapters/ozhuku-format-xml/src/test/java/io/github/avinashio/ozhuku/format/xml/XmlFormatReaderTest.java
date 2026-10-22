package io.github.avinashio.ozhuku.format.xml;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XmlFormatReaderTest {

    @Test
    void shouldReadMultipleRecords() throws Exception {

        final String xml =
                "<records>"
                + "<record><id>1</id><name>Avinash</name></record>"
                + "<record><id>2</id><name>John</name></record>"
                + "</records>";

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));

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
                1L,
                firstRecord.fields().get(0).value());
        assertEquals(
                RecordValueType.INTEGER,
                firstRecord.fields().get(0).type());

        assertEquals(
                "name",
                firstRecord.fields().get(1).name());
        assertEquals(
                "Avinash",
                firstRecord.fields().get(1).value());
        assertEquals(
                RecordValueType.STRING,
                firstRecord.fields().get(1).type());

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

        final String xml =
                "<records>"
                + "<record>"
                + "<active>true</active>"
                + "<deleted></deleted>"
                + "</record>"
                + "</records>";

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));

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
                RecordValueType.BOOLEAN,
                record.fields().get(0).type());

        assertEquals(
                "deleted",
                record.fields().get(1).name());
        assertEquals(
                null,
                record.fields().get(1).value());
        assertEquals(
                RecordValueType.NULL,
                record.fields().get(1).type());

        reader.close();
    }

    @Test
    void shouldReadNumericValues() throws Exception {

        final String xml =
                "<records>"
                + "<record><count>42</count><price>19.95</price></record>"
                + "</records>";

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));

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
                RecordValueType.INTEGER,
                record.fields().get(0).type());

        assertEquals(
                19.95,
                record.fields().get(1).value());
        assertEquals(
                RecordValueType.DECIMAL,
                record.fields().get(1).type());

        reader.close();
    }

    @Test
    void shouldHandleEmptyInput() throws Exception {

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

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
    void shouldHandleEmptyRootElement() throws Exception {

        final String xml = "<records></records>";

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                result.status());

        reader.close();
    }

    @Test
    void shouldInferBooleanValuesCaseInsensitive() throws Exception {

        final String xml =
                "<records>"
                + "<record><flag>TRUE</flag><enabled>FALSE</enabled></record>"
                + "</records>";

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                result.status());

        final Record record =
                result.record();

        assertEquals(
                RecordValueType.BOOLEAN,
                record.fields().get(0).type());
        assertEquals(
                RecordValueType.BOOLEAN,
                record.fields().get(1).type());

        reader.close();
    }

    @Test
    void shouldUseCustomRecordElement() throws Exception {

        final String xml =
                "<rows>"
                + "<row><id>1</id></row>"
                + "<row><id>2</id></row>"
                + "</rows>";

        final XmlReaderConfiguration configuration =
                XmlReaderConfiguration.builder()
                        .recordElement("row")
                        .build();

        final XmlFormatReader reader =
                new XmlFormatReader(configuration);

        reader.open(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));

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
    void shouldRejectReadBeforeOpen() {

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        assertThrows(
                IllegalStateException.class,
                reader::read);
    }

    @Test
    void shouldRejectNullInputStream() {

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        assertThrows(
                NullPointerException.class,
                () -> reader.open(null));
    }

    @Test
    void shouldRejectOpeningReaderTwice()
            throws Exception {

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        final ByteArrayInputStream input =
                new ByteArrayInputStream(
                        "<records><record/></records>"
                                .getBytes(StandardCharsets.UTF_8));

        reader.open(input);

        assertThrows(
                IllegalStateException.class,
                () -> reader.open(
                        new ByteArrayInputStream(
                                "<records/>".getBytes(
                                        StandardCharsets.UTF_8))));

        reader.close();
    }

    @Test
    void shouldHandleSingleRecord() throws Exception {

        final String xml =
                "<records>"
                + "<record><id>99</id><status>active</status></record>"
                + "</records>";

        final XmlFormatReader reader =
                new XmlFormatReader(
                        XmlReaderConfiguration.defaults());

        reader.open(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));

        final FormatReadResult result =
                reader.read();

        assertEquals(
                FormatReadResult.Status.RECORD,
                result.status());

        final Record record =
                result.record();

        assertEquals(
                0,
                record.sequence());
        assertEquals(
                2,
                record.fields().size());

        assertEquals(
                99L,
                record.fields().get(0).value());
        assertEquals(
                RecordValueType.INTEGER,
                record.fields().get(0).type());

        assertEquals(
                "active",
                record.fields().get(1).value());
        assertEquals(
                RecordValueType.STRING,
                record.fields().get(1).type());

        final FormatReadResult end =
                reader.read();

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                end.status());

        reader.close();
    }
}

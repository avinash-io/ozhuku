package io.github.avinashio.ozhuku.domain.record;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordTest {

    @Test
    void shouldCreateRecord() {

        final RecordField field =
                new RecordField(
                        "customerId",
                        RecordValueType.STRING,
                        "C001");

        final Record record =
                new Record(
                        1,
                        List.of(field),
                        RecordMetadata.empty());

        assertEquals(1, record.sequence());
        assertEquals(List.of(field), record.fields());
        assertEquals(
                RecordMetadata.empty(),
                record.metadata());
    }

    @Test
    void shouldRejectNegativeSequence() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Record(
                        -1,
                        List.of(),
                        RecordMetadata.empty()));
    }

    @Test
    void shouldMakeFieldsImmutable() {

        final Record record =
                new Record(
                        1,
                        List.of(
                                new RecordField(
                                        "name",
                                        RecordValueType.STRING,
                                        "Avinash")),
                        RecordMetadata.empty());

        assertThrows(
                UnsupportedOperationException.class,
                () -> record.fields().add(
                        new RecordField(
                                "age",
                                RecordValueType.INTEGER,
                                32)));
    }

    @Test
    void shouldMakeMetadataImmutable() {

        final RecordMetadata metadata =
                new RecordMetadata(
                        Map.of(
                                "source",
                                "test"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> metadata.values().put(
                        "another",
                        "value"));
    }

    @Test
    void shouldSupportNullRecordValue() {

        final RecordField field =
                new RecordField(
                        "middleName",
                        RecordValueType.NULL,
                        null);

        assertTrue(field.value() == null);
        assertEquals(
                RecordValueType.NULL,
                field.type());
    }
}
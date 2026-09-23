package io.github.avinashio.ozhuku.format;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatReadResultTest {

    @Test
    void shouldCreateRecordResult() {

        final Record record =
                new Record(
                        0,
                        List.of(),
                        RecordMetadata.empty());

        final FormatReadResult result =
                FormatReadResult.record(record);

        assertEquals(
                FormatReadResult.Status.RECORD,
                result.status());

        assertEquals(
                record,
                result.record());

        assertNull(result.error());
    }

    @Test
    void shouldCreateEndOfInputResult() {

        final FormatReadResult result =
                FormatReadResult.endOfInput();

        assertEquals(
                FormatReadResult.Status.END_OF_INPUT,
                result.status());

        assertNull(result.record());
        assertNull(result.error());
    }

    @Test
    void shouldCreateErrorResult() {

        final IOException error =
                new IOException("Test failure");

        final FormatReadResult result =
                FormatReadResult.error(error);

        assertEquals(
                FormatReadResult.Status.ERROR,
                result.status());

        assertNull(result.record());
        assertEquals(error, result.error());
    }

    @Test
    void shouldRejectNullRecord() {

        assertThrows(
                ValidationException.class,
                () -> FormatReadResult.record(null));
    }

    @Test
    void shouldRejectNullError() {

        assertThrows(
                ValidationException.class,
                () -> FormatReadResult.error(null));
    }
}

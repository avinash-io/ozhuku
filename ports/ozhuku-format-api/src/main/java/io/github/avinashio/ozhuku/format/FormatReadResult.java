package io.github.avinashio.ozhuku.format;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.foundation.validation.Validation;

public final class FormatReadResult {

    public enum Status {
        RECORD,
        END_OF_INPUT,
        ERROR
    }

    private final Status status;
    private final Record record;
    private final Exception error;

    private FormatReadResult(
            final Status status,
            final Record record,
            final Exception error) {

        this.status = Validation.requireNonNull(
                status,
                "Format read status must not be null");

        this.record = record;
        this.error = error;
    }

    public static FormatReadResult record(
            final Record record) {

        return new FormatReadResult(
                Status.RECORD,
                Validation.requireNonNull(
                        record,
                        "Record must not be null"),
                null);
    }

    public static FormatReadResult endOfInput() {
        return new FormatReadResult(
                Status.END_OF_INPUT,
                null,
                null);
    }

    public static FormatReadResult error(
            final Exception error) {

        return new FormatReadResult(
                Status.ERROR,
                null,
                Validation.requireNonNull(
                        error,
                        "Error must not be null"));
    }

    public Status status() {
        return status;
    }

    public Record record() {
        return record;
    }

    public Exception error() {
        return error;
    }
}
package io.github.avinashio.ozhuku.domain.record;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents one immutable, format-neutral Ozhuku record.
 */
public final class Record {

    private final long sequence;
    private final List<RecordField> fields;
    private final RecordMetadata metadata;

    public Record(
            final long sequence,
            final List<RecordField> fields,
            final RecordMetadata metadata) {

        if (sequence < 0) {
            throw new IllegalArgumentException(
                    "Record sequence must not be negative");
        }

        Validation.requireNonNull(
                fields,
                "Record fields must not be null");

        this.sequence = sequence;
        this.fields = Collections.unmodifiableList(
                List.copyOf(fields));

        this.metadata = Validation.requireNonNull(
                metadata,
                "Record metadata must not be null");
    }

    public long sequence() {
        return sequence;
    }

    public List<RecordField> fields() {
        return fields;
    }

    public RecordMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Record)) {
            return false;
        }

        final Record that = (Record) other;

        return sequence == that.sequence
                && fields.equals(that.fields)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence, fields, metadata);
    }

    @Override
    public String toString() {
        return "Record{"
                + "sequence=" + sequence
                + ", fields=" + fields
                + ", metadata=" + metadata
                + '}';
    }
}
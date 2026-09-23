package io.github.avinashio.ozhuku.domain.record;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Represents one named field within a format-neutral Ozhuku record.
 */
public final class RecordField {

    private final String name;
    private final RecordValueType type;
    private final Object value;

    public RecordField(
            final String name,
            final RecordValueType type,
            final Object value) {

        this.name = Validation.requireNonBlank(
                name,
                "Record field name must not be blank");

        this.type = Validation.requireNonNull(
                type,
                "Record field type must not be null");

        this.value = value;
    }

    public String name() {
        return name;
    }

    public RecordValueType type() {
        return type;
    }

    public Object value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RecordField)) {
            return false;
        }

        final RecordField that = (RecordField) other;

        return name.equals(that.name)
                && type == that.type
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, value);
    }

    @Override
    public String toString() {
        return "RecordField{"
                + "name='" + name + '\''
                + ", type=" + type
                + ", value=" + value
                + '}';
    }
}
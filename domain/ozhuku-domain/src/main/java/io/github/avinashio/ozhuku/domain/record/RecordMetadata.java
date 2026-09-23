package io.github.avinashio.ozhuku.domain.record;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contains metadata associated with a format-neutral Ozhuku record.
 */
public final class RecordMetadata {

    private final Map<String, String> values;

    public RecordMetadata(
            final Map<String, String> values) {

        Validation.requireNonNull(
                values,
                "Record metadata must not be null");

        this.values = Collections.unmodifiableMap(
                new LinkedHashMap<>(values));
    }

    public static RecordMetadata empty() {
        return new RecordMetadata(Map.of());
    }

    public Map<String, String> values() {
        return values;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RecordMetadata)) {
            return false;
        }

        final RecordMetadata that = (RecordMetadata) other;

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "RecordMetadata{"
                + "values=" + values
                + '}';
    }
}
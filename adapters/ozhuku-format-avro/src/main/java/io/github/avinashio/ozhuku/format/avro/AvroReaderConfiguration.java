package io.github.avinashio.ozhuku.format.avro;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Defines configuration used by the Ozhuku Avro reader.
 *
 * <p>Avro files contain a schema embedded in the file header. The reader
 * uses this schema to decode the records. No additional configuration is
 * required beyond the character set for string fields.
 */
public final class AvroReaderConfiguration {

    private static final Charset DEFAULT_CHARSET =
            StandardCharsets.UTF_8;

    private final Charset charset;

    private AvroReaderConfiguration(
            final Charset charset) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");
    }

    public static AvroReaderConfiguration defaults() {
        return new AvroReaderConfiguration(DEFAULT_CHARSET);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Charset charset() {
        return charset;
    }

    public static final class Builder {

        private Charset charset = DEFAULT_CHARSET;

        public Builder charset(
                final Charset charset) {

            this.charset = Objects.requireNonNull(
                    charset,
                    "charset must not be null");

            return this;
        }

        public AvroReaderConfiguration build() {
            return new AvroReaderConfiguration(charset);
        }
    }
}
package io.github.avinashio.ozhuku.format.avro;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Defines configuration used by the Ozhuku Avro writer.
 *
 * <p>The writer requires a schema to write Avro records. The schema can
 * be provided as a JSON string or inferred from the first record written.
 */
public final class AvroWriterConfiguration {

    private static final Charset DEFAULT_CHARSET =
            StandardCharsets.UTF_8;

    private static final String DEFAULT_COMPRESSION_CODEC = "deflate";

    private final Charset charset;
    private final String compressionCodec;
    private final String schemaJson;

    private AvroWriterConfiguration(
            final Charset charset,
            final String compressionCodec,
            final String schemaJson) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");

        this.compressionCodec = Objects.requireNonNull(
                compressionCodec,
                "compressionCodec must not be null");

        this.schemaJson = schemaJson;
    }

    public static AvroWriterConfiguration defaults() {
        return new AvroWriterConfiguration(
                DEFAULT_CHARSET,
                DEFAULT_COMPRESSION_CODEC,
                null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Charset charset() {
        return charset;
    }

    public String compressionCodec() {
        return compressionCodec;
    }

    public String schemaJson() {
        return schemaJson;
    }

    public static final class Builder {

        private Charset charset = DEFAULT_CHARSET;
        private String compressionCodec = DEFAULT_COMPRESSION_CODEC;
        private String schemaJson = null;

        public Builder charset(
                final Charset charset) {

            this.charset = Objects.requireNonNull(
                    charset,
                    "charset must not be null");

            return this;
        }

        public Builder compressionCodec(
                final String compressionCodec) {

            this.compressionCodec = Objects.requireNonNull(
                    compressionCodec,
                    "compressionCodec must not be null");

            return this;
        }

        public Builder schemaJson(
                final String schemaJson) {

            this.schemaJson = schemaJson;
            return this;
        }

        public AvroWriterConfiguration build() {
            return new AvroWriterConfiguration(
                    charset,
                    compressionCodec,
                    schemaJson);
        }
    }
}
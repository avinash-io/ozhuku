package io.github.avinashio.ozhuku.format.parquet;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Configuration for writing Parquet files.
 *
 * <p>Parquet files are self-describing and contain their schema. This
 * configuration supports compression codec selection for optimizing
 * storage size vs. write performance trade-offs.
 */
public final class ParquetWriterConfiguration {

    private final java.nio.charset.Charset charset;
    private final String compressionCodec;

    private ParquetWriterConfiguration(
            final java.nio.charset.Charset charset,
            final String compressionCodec) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");
        this.compressionCodec = Objects.requireNonNull(
                compressionCodec,
                "compressionCodec must not be null");
    }

    /**
     * Returns the default configuration with UTF-8 charset and SNAPPY compression.
     *
     * @return a default configuration instance
     */
    public static ParquetWriterConfiguration defaults() {
        return new ParquetWriterConfiguration(StandardCharsets.UTF_8, "snappy");
    }

    /**
     * Returns a builder for creating a custom configuration.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the configured charset.
     *
     * @return the charset
     */
    public java.nio.charset.Charset charset() {
        return charset;
    }

    /**
     * Returns the configured compression codec.
     *
     * @return the compression codec name (e.g., "snappy", "gzip", "zstd", "lz4", "uncompressed")
     */
    public String compressionCodec() {
        return compressionCodec;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ParquetWriterConfiguration that = (ParquetWriterConfiguration) o;
        return Objects.equals(charset, that.charset) &&
                Objects.equals(compressionCodec, that.compressionCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charset, compressionCodec);
    }

    @Override
    public String toString() {
        return "ParquetWriterConfiguration{" +
                "charset=" + charset +
                ", compressionCodec='" + compressionCodec + '\'' +
                '}';
    }

    /**
     * Builder for ParquetWriterConfiguration.
     */
    public static final class Builder {

        private java.nio.charset.Charset charset = StandardCharsets.UTF_8;
        private String compressionCodec = "snappy";

        private Builder() {
        }

        /**
         * Sets the charset.
         *
         * @param charset the charset to use
         * @return this builder
         */
        public Builder charset(final java.nio.charset.Charset charset) {
            this.charset = Objects.requireNonNull(charset, "charset must not be null");
            return this;
        }

        /**
         * Sets the compression codec.
         *
         * @param compressionCodec the compression codec name (e.g., "snappy", "gzip", "zstd", "lz4", "uncompressed")
         * @return this builder
         */
        public Builder compressionCodec(final String compressionCodec) {
            this.compressionCodec = Objects.requireNonNull(compressionCodec, "compressionCodec must not be null");
            return this;
        }

        /**
         * Builds the configuration.
         *
         * @return a new ParquetWriterConfiguration instance
         */
        public ParquetWriterConfiguration build() {
            return new ParquetWriterConfiguration(charset, compressionCodec);
        }
    }
}
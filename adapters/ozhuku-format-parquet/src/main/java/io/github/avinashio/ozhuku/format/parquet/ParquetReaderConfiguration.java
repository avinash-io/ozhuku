package io.github.avinashio.ozhuku.format.parquet;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Configuration for reading Parquet files.
 *
 * <p>Parquet files are self-describing and contain their schema, so minimal
 * configuration is required. The charset is retained for compatibility with
 * the format adapter interface but is not used for Parquet binary format.
 */
public final class ParquetReaderConfiguration {

    private final java.nio.charset.Charset charset;

    private ParquetReaderConfiguration(
            final java.nio.charset.Charset charset) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");
    }

    /**
     * Returns the default configuration with UTF-8 charset.
     *
     * @return a default configuration instance
     */
    public static ParquetReaderConfiguration defaults() {
        return new ParquetReaderConfiguration(StandardCharsets.UTF_8);
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ParquetReaderConfiguration that = (ParquetReaderConfiguration) o;
        return Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charset);
    }

    @Override
    public String toString() {
        return "ParquetReaderConfiguration{" +
                "charset=" + charset +
                '}';
    }

    /**
     * Builder for ParquetReaderConfiguration.
     */
    public static final class Builder {

        private java.nio.charset.Charset charset = StandardCharsets.UTF_8;

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
         * Builds the configuration.
         *
         * @return a new ParquetReaderConfiguration instance
         */
        public ParquetReaderConfiguration build() {
            return new ParquetReaderConfiguration(charset);
        }
    }
}
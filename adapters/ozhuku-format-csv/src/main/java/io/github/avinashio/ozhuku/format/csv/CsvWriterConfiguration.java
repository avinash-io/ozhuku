package io.github.avinashio.ozhuku.format.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Defines configuration used by the Ozhuku CSV writer.
 */
public final class CsvWriterConfiguration {

    private static final char DEFAULT_DELIMITER = ',';
    private static final char DEFAULT_QUOTE_CHARACTER = '"';
    private static final char DEFAULT_ESCAPE_CHARACTER = '\\';
    private static final Charset DEFAULT_CHARSET =
            StandardCharsets.UTF_8;

    private final char delimiter;
    private final char quoteCharacter;
    private final char escapeCharacter;
    private final Charset charset;
    private final boolean writeHeader;

    private CsvWriterConfiguration(
            final char delimiter,
            final char quoteCharacter,
            final char escapeCharacter,
            final Charset charset,
            final boolean writeHeader) {

        this.delimiter = delimiter;
        this.quoteCharacter = quoteCharacter;
        this.escapeCharacter = escapeCharacter;
        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");
        this.writeHeader = writeHeader;
    }

    public static CsvWriterConfiguration defaults() {
        return new CsvWriterConfiguration(
                DEFAULT_DELIMITER,
                DEFAULT_QUOTE_CHARACTER,
                DEFAULT_ESCAPE_CHARACTER,
                DEFAULT_CHARSET,
                true);
    }

    public static Builder builder() {
        return new Builder();
    }

    public char delimiter() {
        return delimiter;
    }

    public char quoteCharacter() {
        return quoteCharacter;
    }

    public char escapeCharacter() {
        return escapeCharacter;
    }

    public Charset charset() {
        return charset;
    }

    public boolean writeHeader() {
        return writeHeader;
    }

    public static final class Builder {

        private char delimiter = DEFAULT_DELIMITER;
        private char quoteCharacter =
                DEFAULT_QUOTE_CHARACTER;
        private char escapeCharacter =
                DEFAULT_ESCAPE_CHARACTER;
        private Charset charset = DEFAULT_CHARSET;
        private boolean writeHeader = true;

        public Builder delimiter(
                final char delimiter) {

            this.delimiter = delimiter;
            return this;
        }

        public Builder quoteCharacter(
                final char quoteCharacter) {

            this.quoteCharacter = quoteCharacter;
            return this;
        }

        public Builder escapeCharacter(
                final char escapeCharacter) {

            this.escapeCharacter = escapeCharacter;
            return this;
        }

        public Builder charset(
                final Charset charset) {

            this.charset = Objects.requireNonNull(
                    charset,
                    "charset must not be null");

            return this;
        }

        public Builder writeHeader(
                final boolean writeHeader) {

            this.writeHeader = writeHeader;
            return this;
        }

        public CsvWriterConfiguration build() {
            return new CsvWriterConfiguration(
                    delimiter,
                    quoteCharacter,
                    escapeCharacter,
                    charset,
                    writeHeader);
        }
    }
}
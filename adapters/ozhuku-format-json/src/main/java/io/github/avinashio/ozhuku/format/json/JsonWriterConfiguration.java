package io.github.avinashio.ozhuku.format.json;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Defines configuration used by the Ozhuku JSON writer.
 */
public final class JsonWriterConfiguration {

    private static final Charset DEFAULT_CHARSET =
            StandardCharsets.UTF_8;

    private static final boolean DEFAULT_ARRAY_WRAPPED =
            true;

    private static final boolean DEFAULT_PRETTY_PRINT =
            false;

    private final Charset charset;
    private final boolean arrayWrapped;
    private final boolean prettyPrint;

    private JsonWriterConfiguration(
            final Charset charset,
            final boolean arrayWrapped,
            final boolean prettyPrint) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");

        this.arrayWrapped = arrayWrapped;
        this.prettyPrint = prettyPrint;
    }

    public static JsonWriterConfiguration defaults() {
        return new JsonWriterConfiguration(
                DEFAULT_CHARSET,
                DEFAULT_ARRAY_WRAPPED,
                DEFAULT_PRETTY_PRINT);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Charset charset() {
        return charset;
    }

    public boolean arrayWrapped() {
        return arrayWrapped;
    }

    public boolean prettyPrint() {
        return prettyPrint;
    }

    public static final class Builder {

        private Charset charset = DEFAULT_CHARSET;
        private boolean arrayWrapped = DEFAULT_ARRAY_WRAPPED;
        private boolean prettyPrint = DEFAULT_PRETTY_PRINT;

        public Builder charset(
                final Charset charset) {

            this.charset = Objects.requireNonNull(
                    charset,
                    "charset must not be null");

            return this;
        }

        public Builder arrayWrapped(
                final boolean arrayWrapped) {

            this.arrayWrapped = arrayWrapped;
            return this;
        }

        public Builder prettyPrint(
                final boolean prettyPrint) {

            this.prettyPrint = prettyPrint;
            return this;
        }

        public JsonWriterConfiguration build() {
            return new JsonWriterConfiguration(
                    charset,
                    arrayWrapped,
                    prettyPrint);
        }
    }
}

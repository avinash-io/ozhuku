package io.github.avinashio.ozhuku.format.json;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Defines configuration used by the Ozhuku JSON reader.
 */
public final class JsonReaderConfiguration {

    private static final Charset DEFAULT_CHARSET =
            StandardCharsets.UTF_8;

    private static final boolean DEFAULT_ARRAY_WRAPPED =
            true;

    private final Charset charset;
    private final boolean arrayWrapped;

    private JsonReaderConfiguration(
            final Charset charset,
            final boolean arrayWrapped) {

        this.charset = Objects.requireNonNull(
                charset,
                "charset must not be null");

        this.arrayWrapped = arrayWrapped;
    }

    public static JsonReaderConfiguration defaults() {
        return new JsonReaderConfiguration(
                DEFAULT_CHARSET,
                DEFAULT_ARRAY_WRAPPED);
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

    public static final class Builder {

        private Charset charset = DEFAULT_CHARSET;
        private boolean arrayWrapped = DEFAULT_ARRAY_WRAPPED;

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

        public JsonReaderConfiguration build() {
            return new JsonReaderConfiguration(
                    charset,
                    arrayWrapped);
        }
    }
}

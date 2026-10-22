package io.github.avinashio.ozhuku.format.json;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonWriterConfigurationTest {

    @Test
    void shouldCreateDefaults() {

        final JsonWriterConfiguration config =
                JsonWriterConfiguration.defaults();

        assertNotNull(config);
        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());
        assertEquals(
                true,
                config.arrayWrapped());
        assertEquals(
                false,
                config.prettyPrint());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final JsonWriterConfiguration config =
                JsonWriterConfiguration.builder()
                        .charset(StandardCharsets.UTF_16)
                        .arrayWrapped(false)
                        .prettyPrint(true)
                        .build();

        assertEquals(
                StandardCharsets.UTF_16,
                config.charset());
        assertEquals(
                false,
                config.arrayWrapped());
        assertEquals(
                true,
                config.prettyPrint());
    }

    @Test
    void shouldRejectNullCharset() {

        assertThrows(
                NullPointerException.class,
                () -> JsonWriterConfiguration.builder()
                        .charset(null)
                        .build());
    }
}

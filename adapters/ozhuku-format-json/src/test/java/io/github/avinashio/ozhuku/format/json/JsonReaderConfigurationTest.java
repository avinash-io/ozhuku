package io.github.avinashio.ozhuku.format.json;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonReaderConfigurationTest {

    @Test
    void shouldCreateDefaults() {

        final JsonReaderConfiguration config =
                JsonReaderConfiguration.defaults();

        assertNotNull(config);
        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());
        assertEquals(
                true,
                config.arrayWrapped());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final JsonReaderConfiguration config =
                JsonReaderConfiguration.builder()
                        .charset(StandardCharsets.UTF_16)
                        .arrayWrapped(false)
                        .build();

        assertEquals(
                StandardCharsets.UTF_16,
                config.charset());
        assertEquals(
                false,
                config.arrayWrapped());
    }

    @Test
    void shouldRejectNullCharset() {

        assertThrows(
                NullPointerException.class,
                () -> JsonReaderConfiguration.builder()
                        .charset(null)
                        .build());
    }
}

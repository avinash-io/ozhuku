package io.github.avinashio.ozhuku.format.avro;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvroReaderConfigurationTest {

    @Test
    void shouldCreateDefaults() {

        final AvroReaderConfiguration config =
                AvroReaderConfiguration.defaults();

        assertNotNull(config);
        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final AvroReaderConfiguration config =
                AvroReaderConfiguration.builder()
                        .charset(StandardCharsets.UTF_16)
                        .build();

        assertEquals(
                StandardCharsets.UTF_16,
                config.charset());
    }

    @Test
    void shouldRejectNullCharset() {

        assertThrows(
                NullPointerException.class,
                () -> AvroReaderConfiguration.builder()
                        .charset(null)
                        .build());
    }
}
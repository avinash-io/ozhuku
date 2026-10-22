package io.github.avinashio.ozhuku.format.parquet;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParquetReaderConfigurationTest {

    @Test
    void shouldCreateDefaults() {

        final ParquetReaderConfiguration config =
                ParquetReaderConfiguration.defaults();

        assertNotNull(config);
        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final ParquetReaderConfiguration config =
                ParquetReaderConfiguration.builder()
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
                () -> ParquetReaderConfiguration.builder()
                        .charset(null)
                        .build());
    }
}
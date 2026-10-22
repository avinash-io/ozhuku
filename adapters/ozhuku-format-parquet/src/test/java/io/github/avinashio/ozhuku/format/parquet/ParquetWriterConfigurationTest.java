package io.github.avinashio.ozhuku.format.parquet;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParquetWriterConfigurationTest {

    @Test
    void shouldCreateDefaults() {

        final ParquetWriterConfiguration config =
                ParquetWriterConfiguration.defaults();

        assertNotNull(config);
        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());
        assertEquals(
                "snappy",
                config.compressionCodec());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final ParquetWriterConfiguration config =
                ParquetWriterConfiguration.builder()
                        .charset(StandardCharsets.UTF_16)
                        .compressionCodec("gzip")
                        .build();

        assertEquals(
                StandardCharsets.UTF_16,
                config.charset());
        assertEquals(
                "gzip",
                config.compressionCodec());
    }

    @Test
    void shouldRejectNullCharset() {

        assertThrows(
                NullPointerException.class,
                () -> ParquetWriterConfiguration.builder()
                        .charset(null)
                        .build());
    }

    @Test
    void shouldRejectNullCompressionCodec() {

        assertThrows(
                NullPointerException.class,
                () -> ParquetWriterConfiguration.builder()
                        .compressionCodec(null)
                        .build());
    }
}
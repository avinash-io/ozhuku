package io.github.avinashio.ozhuku.format.avro;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvroWriterConfigurationTest {

    @Test
    void shouldCreateDefaults() {

        final AvroWriterConfiguration config =
                AvroWriterConfiguration.defaults();

        assertNotNull(config);
        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());
        assertEquals(
                "deflate",
                config.compressionCodec());
        assertEquals(
                null,
                config.schemaJson());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final AvroWriterConfiguration config =
                AvroWriterConfiguration.builder()
                        .charset(StandardCharsets.UTF_16)
                        .compressionCodec("snappy")
                        .schemaJson("{\"type\":\"record\",\"name\":\"Test\",\"fields\":[]}")
                        .build();

        assertEquals(
                StandardCharsets.UTF_16,
                config.charset());
        assertEquals(
                "snappy",
                config.compressionCodec());
        assertEquals(
                "{\"type\":\"record\",\"name\":\"Test\",\"fields\":[]}",
                config.schemaJson());
    }

    @Test
    void shouldRejectNullCharset() {

        assertThrows(
                NullPointerException.class,
                () -> AvroWriterConfiguration.builder()
                        .charset(null)
                        .build());
    }

    @Test
    void shouldRejectNullCompressionCodec() {

        assertThrows(
                NullPointerException.class,
                () -> AvroWriterConfiguration.builder()
                        .compressionCodec(null)
                        .build());
    }
}
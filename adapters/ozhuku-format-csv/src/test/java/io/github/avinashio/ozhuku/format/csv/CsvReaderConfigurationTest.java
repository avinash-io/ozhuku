package io.github.avinashio.ozhuku.format.csv;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvReaderConfigurationTest {

    @Test
    void shouldProvideDefaultConfiguration() {

        final CsvReaderConfiguration configuration =
                CsvReaderConfiguration.defaults();

        assertEquals(
                ',',
                configuration.delimiter());

        assertEquals(
                '"',
                configuration.quoteCharacter());

        assertEquals(
                '\\',
                configuration.escapeCharacter());

        assertEquals(
                StandardCharsets.UTF_8,
                configuration.charset());

        assertTrue(
                configuration.headerPresent());
    }

    @Test
    void shouldBuildCustomConfiguration() {

        final CsvReaderConfiguration configuration =
                CsvReaderConfiguration.builder()
                        .delimiter(';')
                        .quoteCharacter('\'')
                        .escapeCharacter('\\')
                        .charset(StandardCharsets.ISO_8859_1)
                        .headerPresent(false)
                        .build();

        assertEquals(
                ';',
                configuration.delimiter());

        assertEquals(
                '\'',
                configuration.quoteCharacter());

        assertEquals(
                '\\',
                configuration.escapeCharacter());

        assertEquals(
                StandardCharsets.ISO_8859_1,
                configuration.charset());

        assertEquals(
                false,
                configuration.headerPresent());
    }

    @Test
    void shouldRejectNullCharset() {

        assertThrows(
                NullPointerException.class,
                () -> CsvReaderConfiguration.builder()
                        .charset(null));
    }
}
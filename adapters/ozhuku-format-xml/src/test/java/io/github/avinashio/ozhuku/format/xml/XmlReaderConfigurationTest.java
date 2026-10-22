package io.github.avinashio.ozhuku.format.xml;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XmlReaderConfigurationTest {

    @Test
    void shouldReturnDefaults() {

        final XmlReaderConfiguration config =
                XmlReaderConfiguration.defaults();

        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());

        assertEquals(
                "record",
                config.recordElement());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final XmlReaderConfiguration config =
                XmlReaderConfiguration.builder()
                        .charset(StandardCharsets.ISO_8859_1)
                        .recordElement("row")
                        .build();

        assertEquals(
                StandardCharsets.ISO_8859_1,
                config.charset());

        assertEquals(
                "row",
                config.recordElement());
    }

    @Test
    void shouldRejectNullCharset() {

        final XmlReaderConfiguration.Builder builder =
                XmlReaderConfiguration.builder();

        assertThrows(
                NullPointerException.class,
                () -> builder.charset(null));
    }

    @Test
    void shouldRejectNullRecordElement() {

        final XmlReaderConfiguration.Builder builder =
                XmlReaderConfiguration.builder();

        assertThrows(
                NullPointerException.class,
                () -> builder.recordElement(null));
    }

    @Test
    void shouldBuildWithArbitraryCharset() {

        final Charset customCharset =
                Charset.forName("US-ASCII");

        final XmlReaderConfiguration config =
                XmlReaderConfiguration.builder()
                        .charset(customCharset)
                        .build();

        assertEquals(
                customCharset,
                config.charset());
    }
}

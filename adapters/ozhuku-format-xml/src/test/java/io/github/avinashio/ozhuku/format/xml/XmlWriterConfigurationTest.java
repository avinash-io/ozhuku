package io.github.avinashio.ozhuku.format.xml;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlWriterConfigurationTest {

    @Test
    void shouldReturnDefaults() {

        final XmlWriterConfiguration config =
                XmlWriterConfiguration.defaults();

        assertEquals(
                StandardCharsets.UTF_8,
                config.charset());

        assertEquals(
                "records",
                config.rootElement());

        assertEquals(
                "record",
                config.recordElement());

        assertFalse(config.prettyPrint());
    }

    @Test
    void shouldBuildWithCustomValues() {

        final XmlWriterConfiguration config =
                XmlWriterConfiguration.builder()
                        .rootElement("rows")
                        .recordElement("row")
                        .prettyPrint(true)
                        .build();

        assertEquals(
                "rows",
                config.rootElement());

        assertEquals(
                "row",
                config.recordElement());

        assertTrue(config.prettyPrint());
    }

    @Test
    void shouldRejectNullRootElement() {

        final XmlWriterConfiguration.Builder builder =
                XmlWriterConfiguration.builder();

        assertThrows(
                NullPointerException.class,
                () -> builder.rootElement(null));
    }

    @Test
    void shouldRejectNullRecordElement() {

        final XmlWriterConfiguration.Builder builder =
                XmlWriterConfiguration.builder();

        assertThrows(
                NullPointerException.class,
                () -> builder.recordElement(null));
    }
}

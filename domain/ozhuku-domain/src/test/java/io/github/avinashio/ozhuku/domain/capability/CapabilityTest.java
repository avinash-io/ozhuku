package io.github.avinashio.ozhuku.domain.capability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CapabilityTest {

    @Test
    void shouldContainExactlyFiveCapabilities() {
        assertEquals(5, Capability.values().length);
    }

    @Test
    void shouldContainCreate() {
        assertNotNull(Capability.valueOf("CREATE"));
    }

    @Test
    void shouldContainReplace() {
        assertNotNull(Capability.valueOf("REPLACE"));
    }

    @Test
    void shouldContainAppend() {
        assertNotNull(Capability.valueOf("APPEND"));
    }

    @Test
    void shouldContainDelete() {
        assertNotNull(Capability.valueOf("DELETE"));
    }

    @Test
    void shouldContainVersion() {
        assertNotNull(Capability.valueOf("VERSION"));
    }
}
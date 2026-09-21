package io.github.avinashio.ozhuku.domain.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class FlowModeTest {

    @Test
    void shouldContainExactlyThreeSupportedModes() {
        assertEquals(3, FlowMode.values().length);
    }

    @Test
    void shouldContainResourceTransfer() {
        assertNotNull(FlowMode.valueOf("RESOURCE_TRANSFER"));
    }

    @Test
    void shouldContainRecordProcessing() {
        assertNotNull(FlowMode.valueOf("RECORD_PROCESSING"));
    }

    @Test
    void shouldContainResourceProcessing() {
        assertNotNull(FlowMode.valueOf("RESOURCE_PROCESSING"));
    }
}
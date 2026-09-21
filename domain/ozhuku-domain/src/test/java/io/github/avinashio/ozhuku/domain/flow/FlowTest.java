package io.github.avinashio.ozhuku.domain.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class FlowTest {

    @Test
    void shouldCreateFlow() {
        final FlowId flowId = new FlowId("customer-transfer");

        final Flow flow = new Flow(
                flowId,
                "Customer Transfer",
                FlowMode.RESOURCE_TRANSFER);

        assertEquals(flowId, flow.id());
        assertEquals("Customer Transfer", flow.name());
        assertEquals(FlowMode.RESOURCE_TRANSFER, flow.mode());
    }

    @Test
    void shouldSupportRecordProcessing() {
        final Flow flow = new Flow(
                new FlowId("customer-processing"),
                "Customer Processing",
                FlowMode.RECORD_PROCESSING);

        assertEquals(FlowMode.RECORD_PROCESSING, flow.mode());
    }

    @Test
    void shouldSupportResourceProcessing() {
        final Flow flow = new Flow(
                new FlowId("resource-processing"),
                "Resource Processing",
                FlowMode.RESOURCE_PROCESSING);

        assertEquals(FlowMode.RESOURCE_PROCESSING, flow.mode());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(
                ValidationException.class,
                () -> new Flow(
                        null,
                        "Customer Transfer",
                        FlowMode.RESOURCE_TRANSFER));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(
                ValidationException.class,
                () -> new Flow(
                        new FlowId("customer-transfer"),
                        "   ",
                        FlowMode.RESOURCE_TRANSFER));
    }

    @Test
    void shouldRejectNullMode() {
        assertThrows(
                ValidationException.class,
                () -> new Flow(
                        new FlowId("customer-transfer"),
                        "Customer Transfer",
                        null));
    }
}
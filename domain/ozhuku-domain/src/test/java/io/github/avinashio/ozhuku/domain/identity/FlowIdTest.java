package io.github.avinashio.ozhuku.domain.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import org.junit.jupiter.api.Test;

class FlowIdTest {

    @Test
    void shouldCreateFlowId() {
        final FlowId flowId = new FlowId("customer-transfer");

        assertEquals("customer-transfer", flowId.value());
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                ValidationException.class,
                () -> new FlowId(null));
    }

    @Test
    void shouldRejectBlankValue() {
        assertThrows(
                ValidationException.class,
                () -> new FlowId("   "));
    }

    @Test
    void shouldCompareByValue() {
        final FlowId first = new FlowId("customer-transfer");
        final FlowId second = new FlowId("customer-transfer");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualPipelineId() {
        final FlowId flowId = new FlowId("customer-transfer");
        final PipelineId pipelineId = new PipelineId("customer-transfer");

        assertNotEquals(flowId, pipelineId);
    }

    @Test
    void shouldReturnValueFromToString() {
        final FlowId flowId = new FlowId("customer-transfer");

        assertEquals("customer-transfer", flowId.toString());
    }
}
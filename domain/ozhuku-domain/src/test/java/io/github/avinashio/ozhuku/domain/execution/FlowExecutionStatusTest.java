package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class FlowExecutionStatusTest {

    @Test
    void shouldContainAllSupportedFlowExecutionStatuses() {
        assertEquals(5, FlowExecutionStatus.values().length);

        assertNotNull(FlowExecutionStatus.valueOf("PENDING"));
        assertNotNull(FlowExecutionStatus.valueOf("RUNNING"));
        assertNotNull(FlowExecutionStatus.valueOf("COMPLETED"));
        assertNotNull(FlowExecutionStatus.valueOf("FAILED"));
        assertNotNull(FlowExecutionStatus.valueOf("CANCELLED"));
    }
}
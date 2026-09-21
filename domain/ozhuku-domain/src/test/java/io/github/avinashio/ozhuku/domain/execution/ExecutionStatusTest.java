package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ExecutionStatusTest {

    @Test
    void shouldContainAllSupportedExecutionStatuses() {
        assertEquals(5, ExecutionStatus.values().length);

        assertNotNull(ExecutionStatus.valueOf("PENDING"));
        assertNotNull(ExecutionStatus.valueOf("RUNNING"));
        assertNotNull(ExecutionStatus.valueOf("COMPLETED"));
        assertNotNull(ExecutionStatus.valueOf("FAILED"));
        assertNotNull(ExecutionStatus.valueOf("CANCELLED"));
    }
}
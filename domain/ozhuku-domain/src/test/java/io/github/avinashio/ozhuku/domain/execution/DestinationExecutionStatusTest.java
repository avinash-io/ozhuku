package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DestinationExecutionStatusTest {

    @Test
    void shouldContainAllSupportedDestinationExecutionStatuses() {
        assertEquals(5, DestinationExecutionStatus.values().length);

        assertNotNull(DestinationExecutionStatus.valueOf("PENDING"));
        assertNotNull(DestinationExecutionStatus.valueOf("RUNNING"));
        assertNotNull(DestinationExecutionStatus.valueOf("COMPLETED"));
        assertNotNull(DestinationExecutionStatus.valueOf("FAILED"));
        assertNotNull(DestinationExecutionStatus.valueOf("CANCELLED"));
    }
}
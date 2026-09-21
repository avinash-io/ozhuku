package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SourceExecutionStatusTest {

    @Test
    void shouldContainAllSupportedSourceExecutionStatuses() {
        assertEquals(5, SourceExecutionStatus.values().length);

        assertNotNull(SourceExecutionStatus.valueOf("PENDING"));
        assertNotNull(SourceExecutionStatus.valueOf("RUNNING"));
        assertNotNull(SourceExecutionStatus.valueOf("COMPLETED"));
        assertNotNull(SourceExecutionStatus.valueOf("FAILED"));
        assertNotNull(SourceExecutionStatus.valueOf("CANCELLED"));
    }
}
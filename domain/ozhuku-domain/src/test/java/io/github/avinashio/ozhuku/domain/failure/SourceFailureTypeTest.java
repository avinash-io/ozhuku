package io.github.avinashio.ozhuku.domain.failure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SourceFailureTypeTest {

    @Test
    void shouldContainAllSupportedSourceFailureTypes() {
        assertEquals(4, SourceFailureType.values().length);

        assertNotNull(SourceFailureType.valueOf("SOURCE_UNAVAILABLE"));
        assertNotNull(SourceFailureType.valueOf("SOURCE_CHANGED"));
        assertNotNull(SourceFailureType.valueOf("SOURCE_INCOMPLETE"));
        assertNotNull(SourceFailureType.valueOf("SOURCE_LOCKED"));
    }
}
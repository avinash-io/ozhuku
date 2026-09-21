package io.github.avinashio.ozhuku.domain.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConflictBehaviorTest {

    @Test
    void shouldContainAllSupportedConflictBehaviors() {
        assertEquals(4, ConflictBehavior.values().length);

        assertNotNull(ConflictBehavior.valueOf("FAIL"));
        assertNotNull(ConflictBehavior.valueOf("REPLACE"));
        assertNotNull(ConflictBehavior.valueOf("SKIP"));
        assertNotNull(ConflictBehavior.valueOf("VERSION"));
    }
}
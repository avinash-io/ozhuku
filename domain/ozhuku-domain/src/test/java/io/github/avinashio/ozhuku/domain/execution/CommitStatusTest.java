package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CommitStatusTest {

    @Test
    void shouldContainAllSupportedCommitStatuses() {
        assertEquals(3, CommitStatus.values().length);

        assertNotNull(CommitStatus.valueOf("NOT_COMMITTED"));
        assertNotNull(CommitStatus.valueOf("COMMITTED"));
        assertNotNull(CommitStatus.valueOf("UNKNOWN"));
    }
}
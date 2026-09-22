package io.github.avinashio.ozhuku.domain.deduplication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DuplicatePolicyTest {

    @Test
    void shouldDefineAllSupportedPolicies() {
        assertEquals(
                4,
                DuplicatePolicy.values().length);
    }

    @Test
    void shouldExposePoliciesInExpectedOrder() {
        assertEquals(
                DuplicatePolicy.SKIP_IF_PROCESSED,
                DuplicatePolicy.values()[0]);

        assertEquals(
                DuplicatePolicy.REPROCESS_IF_CHANGED,
                DuplicatePolicy.values()[1]);

        assertEquals(
                DuplicatePolicy.ALWAYS_PROCESS,
                DuplicatePolicy.values()[2]);

        assertEquals(
                DuplicatePolicy.FAIL_IF_DUPLICATE,
                DuplicatePolicy.values()[3]);
    }
}
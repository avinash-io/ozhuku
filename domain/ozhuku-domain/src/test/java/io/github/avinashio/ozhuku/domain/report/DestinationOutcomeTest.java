package io.github.avinashio.ozhuku.domain.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.avinashio.ozhuku.domain.deduplication.ProcessingStatus;
import org.junit.jupiter.api.Test;

class DestinationOutcomeTest {

    @Test
    void shouldCreateDestinationOutcome() {
        final DestinationOutcome outcome =
                new DestinationOutcome(
                        "resource-001",
                        ProcessingStatus.PROCESSED,
                        "Processed successfully");

        assertEquals("resource-001", outcome.resourceId());
        assertEquals(ProcessingStatus.PROCESSED, outcome.status());
        assertEquals("Processed successfully", outcome.detail());
    }

    @Test
    void shouldAllowNullDetail() {
        final DestinationOutcome outcome =
                new DestinationOutcome(
                        "resource-002",
                        ProcessingStatus.NOT_PROCESSED,
                        null);

        assertNull(outcome.detail());
    }

    @Test
    void shouldCompareByResourceId() {
        final DestinationOutcome first =
                new DestinationOutcome(
                        "resource-001",
                        ProcessingStatus.PROCESSED,
                        "ok");

        final DestinationOutcome second =
                new DestinationOutcome(
                        "resource-001",
                        ProcessingStatus.NOT_PROCESSED,
                        "failed");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentResourceId() {
        final DestinationOutcome first =
                new DestinationOutcome(
                        "resource-001",
                        ProcessingStatus.PROCESSED,
                        "ok");

        final DestinationOutcome second =
                new DestinationOutcome(
                        "resource-002",
                        ProcessingStatus.PROCESSED,
                        "ok");

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotEqualNull() {
        final DestinationOutcome outcome =
                new DestinationOutcome(
                        "resource-001",
                        ProcessingStatus.PROCESSED,
                        "ok");

        assertNotEquals(outcome, null);
    }

    @Test
    void shouldNotEqualOtherType() {
        final DestinationOutcome outcome =
                new DestinationOutcome(
                        "resource-001",
                        ProcessingStatus.PROCESSED,
                        "ok");

        assertNotEquals(outcome, "some-string");
    }

    @Test
    void shouldReturnValueFromToString() {
        final DestinationOutcome outcome =
                new DestinationOutcome(
                        "resource-001",
                        ProcessingStatus.PROCESSED,
                        "ok");

        final String str = outcome.toString();
        assertEquals(true, str.contains("resource-001"));
        assertEquals(true, str.contains("PROCESSED"));
        assertEquals(true, str.contains("ok"));
    }
}

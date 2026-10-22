package io.github.avinashio.ozhuku.acquisition;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PushResourceTest {

    @Test
    void shouldCreateReceivedResource() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                "input.csv",
                1024L,
                "text/csv",
                1234567890L,
                "checksum-abc");

        assertEquals("push-1", resource.resourceId().value());
        assertEquals(
                "file:///staging/push-1.dat",
                resource.stagingLocation().value());
        assertEquals("input.csv", resource.originalFilename());
        assertEquals(1024L, resource.size());
        assertEquals("text/csv", resource.contentType());
        assertEquals(1234567890L, resource.receivedAt());
        assertEquals("checksum-abc", resource.checksum());
        assertEquals(PushResourceStatus.RECEIVED, resource.status());
    }

    @Test
    void shouldTransitionFromReceivedToAvailable() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                "input.csv",
                1024L,
                "text/csv",
                1234567890L,
                "checksum-abc");

        final PushResource available = resource.markAvailable();

        assertEquals(PushResourceStatus.AVAILABLE, available.status());
        assertEquals("push-1", available.resourceId().value());
        assertEquals(1024L, available.size());
    }

    @Test
    void shouldTransitionFromAvailableToProcessing() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                "input.csv",
                1024L,
                "text/csv",
                1234567890L,
                "checksum-abc")
                .markAvailable();

        final PushResource processing = resource.markProcessing();

        assertEquals(PushResourceStatus.PROCESSING, processing.status());
    }

    @Test
    void shouldTransitionFromProcessingToCompleted() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                "input.csv",
                1024L,
                "text/csv",
                1234567890L,
                "checksum-abc")
                .markAvailable()
                .markProcessing();

        final PushResource completed = resource.markCompleted();

        assertEquals(PushResourceStatus.COMPLETED, completed.status());
    }

    @Test
    void shouldRejectMarkAvailableFromNonReceived() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                null,
                0L,
                null,
                0L,
                null)
                .markAvailable();

        assertThrows(
                IllegalStateException.class,
                resource::markAvailable);
    }

    @Test
    void shouldRejectMarkProcessingFromNonAvailable() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                null,
                0L,
                null,
                0L,
                null);

        assertThrows(
                IllegalStateException.class,
                resource::markProcessing);
    }

    @Test
    void shouldRejectMarkCompletedFromNonProcessing() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                null,
                0L,
                null,
                0L,
                null)
                .markAvailable();

        assertThrows(
                IllegalStateException.class,
                resource::markCompleted);
    }

    @Test
    void shouldRejectNullResourceId() {

        assertThrows(
                NullPointerException.class,
                () -> PushResource.received(
                        null,
                        new ResourceLocation("file:///test.dat"),
                        null,
                        0L,
                        null,
                        0L,
                        null));
    }

    @Test
    void shouldRejectNullStagingLocation() {

        assertThrows(
                NullPointerException.class,
                () -> PushResource.received(
                        new ResourceId("push-1"),
                        null,
                        null,
                        0L,
                        null,
                        0L,
                        null));
    }

    @Test
    void shouldValidateRequiredFields() {

        final PushResource resource = PushResource.received(
                new ResourceId("push-1"),
                new ResourceLocation("file:///staging/push-1.dat"),
                "input.csv",
                1024L,
                "text/csv",
                1234567890L,
                "checksum-abc");

        assertNotNull(resource.resourceId());
        assertNotNull(resource.stagingLocation());
        assertNotNull(resource.status());
    }
}

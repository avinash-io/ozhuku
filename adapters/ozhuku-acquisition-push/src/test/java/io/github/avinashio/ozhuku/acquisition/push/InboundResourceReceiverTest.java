package io.github.avinashio.ozhuku.acquisition.push;

import io.github.avinashio.ozhuku.acquisition.PushResource;
import io.github.avinashio.ozhuku.acquisition.PushResourceStatus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InboundResourceReceiverTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldStageResourceAndComputeChecksum() throws IOException {

        final TestPushResourceRepository repository =
                new TestPushResourceRepository();

        final InboundResourceReceiver receiver =
                new InboundResourceReceiver(
                        tempDir,
                        repository,
                        1024L);

        final byte[] content = "hello-push".getBytes(StandardCharsets.UTF_8);

        final PushResource resource = receiver.receive(
                new ByteArrayInputStream(content),
                "input.txt",
                "text/plain",
                "sender-1",
                "msg-1");

        assertNotNull(resource.resourceId());
        assertEquals(PushResourceStatus.RECEIVED, resource.status());
        assertEquals("input.txt", resource.originalFilename());
        assertEquals("text/plain", resource.contentType());
        assertEquals(content.length, resource.size());
        assertNotNull(resource.checksum());
        assertTrue(repository.saved);
        assertTrue(Files.exists(
                tempDir.resolve(resource.resourceId().value() + ".dat")));
    }

    @Test
    void shouldRejectOversizedPayload() throws IOException {

        final TestPushResourceRepository repository =
                new TestPushResourceRepository();

        final InboundResourceReceiver receiver =
                new InboundResourceReceiver(
                        tempDir,
                        repository,
                        10L);

        final byte[] content = "this-is-too-big".getBytes(StandardCharsets.UTF_8);

        assertThrows(
                IOException.class,
                () -> receiver.receive(
                        new ByteArrayInputStream(content),
                        null,
                        null,
                        "sender-1",
                        "msg-1"));
    }

    @Test
    void shouldRejectNullContent() {

        final TestPushResourceRepository repository =
                new TestPushResourceRepository();

        final InboundResourceReceiver receiver =
                new InboundResourceReceiver(
                        tempDir,
                        repository,
                        1024L);

        assertThrows(
                NullPointerException.class,
                () -> receiver.receive(
                        null,
                        "file.txt",
                        "text/plain",
                        "sender-1",
                        "msg-1"));
    }
}

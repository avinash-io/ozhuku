package io.github.avinashio.ozhuku.application.transfer;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.storage.StorageReader;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceTransferServiceTest {

    @Test
    void shouldTransferResourceContent()
            throws IOException {

        final byte[] expected =
                "ozhuku-transfer-test".getBytes();

        final TestStorageReader reader =
                new TestStorageReader(expected);

        final TestStorageWriter writer =
                new TestStorageWriter();

        final ResourceTransferService service =
                new ResourceTransferService(
                        reader,
                        writer);

        final Resource source =
                resource("source-1");

        final Resource destination =
                resource("destination-1");

        final DeliveryPolicy policy =
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE);

        service.transfer(
                source,
                destination,
                policy);

        assertArrayEquals(
                expected,
                writer.content());

        assertArrayEquals(
                expected,
                writer.receivedContent());

        if (writer.conflictBehavior()
                != ConflictBehavior.REPLACE) {
            throw new AssertionError(
                    "Expected REPLACE conflict behavior");
        }
    }

    @Test
    void shouldRejectNullSource() {
        final ResourceTransferService service =
                new ResourceTransferService(
                        new TestStorageReader(
                                "content".getBytes()),
                        new TestStorageWriter());

        final Resource destination =
                resource("destination-1");

        final DeliveryPolicy policy =
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE);

        assertThrows(
                NullPointerException.class,
                () -> service.transfer(
                        null,
                        destination,
                        policy));
    }

    @Test
    void shouldRejectNullDestination() {
        final ResourceTransferService service =
                new ResourceTransferService(
                        new TestStorageReader(
                                "content".getBytes()),
                        new TestStorageWriter());

        final Resource source =
                resource("source-1");

        final DeliveryPolicy policy =
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE);

        assertThrows(
                NullPointerException.class,
                () -> service.transfer(
                        source,
                        null,
                        policy));
    }

    @Test
    void shouldRejectNullDeliveryPolicy() {
        final ResourceTransferService service =
                new ResourceTransferService(
                        new TestStorageReader(
                                "content".getBytes()),
                        new TestStorageWriter());

        final Resource source =
                resource("source-1");

        final Resource destination =
                resource("destination-1");

        assertThrows(
                NullPointerException.class,
                () -> service.transfer(
                        source,
                        destination,
                        null));
    }

    @Test
    void shouldPropagateReaderFailure() {

        final IOException expected =
                new IOException(
                        "Simulated reader failure");

        final StorageReader reader =
                resource -> {
                    throw expected;
                };

        final TestStorageWriter writer =
                new TestStorageWriter();

        final ResourceTransferService service =
                new ResourceTransferService(
                        reader,
                        writer);

        final Resource source =
                resource("source-1");

        final Resource destination =
                resource("destination-1");

        final DeliveryPolicy policy =
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE);

        final IOException actual =
                assertThrows(
                        IOException.class,
                        () -> service.transfer(
                                source,
                                destination,
                                policy));

        if (actual != expected) {
            throw new AssertionError(
                    "Expected the original reader exception");
        }

        if (writer.wasCalled()) {
            throw new AssertionError(
                    "Writer must not be called when reader "
                            + "fails to open the source");
        }
    }

    private static Resource resource(
            final String id) {

        return new Resource(
                new ResourceId(id),
                new ResourceLocation(
                        "file:///"
                                + id));
    }

    private static final class TestStorageReader
            implements StorageReader {

        private final byte[] content;

        private TestStorageReader(
                final byte[] content) {
            this.content = content.clone();
        }

        @Override
        public InputStream open(
                final Resource resource) {

            return new ByteArrayInputStream(
                    content);
        }
    }

    private static final class TestStorageWriter
            implements StorageWriter {

        private final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        private ConflictBehavior conflictBehavior;

        private boolean called;

        @Override
        public void write(
                final Resource destination,
                final InputStream content,
                final ConflictBehavior conflictBehavior)
                throws IOException {

            this.called = true;
            this.conflictBehavior =
                    conflictBehavior;

            content.transferTo(output);
        }

        private byte[] content() {
            return output.toByteArray();
        }

        private byte[] receivedContent() {
            return output.toByteArray();
        }

        private ConflictBehavior conflictBehavior() {
            return conflictBehavior;
        }

        private boolean wasCalled() {
            return called;
        }
    }
}
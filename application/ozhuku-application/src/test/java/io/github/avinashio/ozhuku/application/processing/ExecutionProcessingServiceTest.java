package io.github.avinashio.ozhuku.application.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.application.record.RecordProcessingService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import io.github.avinashio.ozhuku.storage.StorageOutputProvider;
import io.github.avinashio.ozhuku.storage.StorageReader;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExecutionProcessingServiceTest {

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    @Test
    void processResourceTransferShouldDelegateToTransferService()
            throws IOException {

        final byte[] expectedContent =
                "execution-processing-test"
                        .getBytes();

        final TestStorageReader reader =
                new TestStorageReader(expectedContent);

        final TestStorageWriter writer =
                new TestStorageWriter();

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        reader,
                        writer);

        final RecordProcessingService recordProcessingService =
                new RecordProcessingService(
                        new TestStorageReader(
                                "unused".getBytes()),
                        new TestStorageOutputProvider());

        final ExecutionProcessingService service =
                new ExecutionProcessingService(
                        transferService,
                        recordProcessingService);

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE));

        service.processResourceTransfer(
                EXECUTION_ID,
                request);

        assertTrue(writer.called());
        assertEquals(
                ConflictBehavior.REPLACE,
                writer.conflictBehavior());

        assertEquals(
                new String(expectedContent),
                new String(writer.content()));
    }

    @Test
    void processRecordProcessingShouldDelegateToRecordProcessingService()
            throws IOException {

        final Record firstRecord =
                record(0, "first");

        final Record secondRecord =
                record(1, "second");

        final TestStorageReader storageReader =
                new TestStorageReader(
                        "input".getBytes());

        final TestStorageOutputProvider outputProvider =
                new TestStorageOutputProvider();

        final TestFormatReader formatReader =
                new TestFormatReader(
                        firstRecord,
                        secondRecord);

        final TestFormatWriter formatWriter =
                new TestFormatWriter();

        final RecordProcessingService recordProcessingService =
                new RecordProcessingService(
                        storageReader,
                        outputProvider);

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        new TestStorageReader(
                                "unused".getBytes()),
                        new TestStorageWriter());

        final ExecutionProcessingService service =
                new ExecutionProcessingService(
                        transferService,
                        recordProcessingService);

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE),
                        formatReader,
                        formatWriter);

        service.processRecordProcessing(
                EXECUTION_ID,
                request);

        assertEquals(
                List.of(firstRecord, secondRecord),
                formatWriter.records());

        assertTrue(formatReader.opened());
        assertTrue(formatReader.closed());
        assertTrue(formatWriter.opened());
        assertTrue(formatWriter.closed());
        assertTrue(outputProvider.outputCommitted());
        assertTrue(outputProvider.outputClosed());
        assertTrue(storageReader.inputClosed());
    }

    @Test
    void processResourceTransferShouldPropagateFailure()
            throws IOException {

        final IOException expected =
                new IOException("Transfer failed");

        final StorageReader reader =
                resource -> {
                    throw expected;
                };

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        reader,
                        new TestStorageWriter());

        final RecordProcessingService recordProcessingService =
                new RecordProcessingService(
                        new TestStorageReader(
                                "unused".getBytes()),
                        new TestStorageOutputProvider());

        final ExecutionProcessingService service =
                new ExecutionProcessingService(
                        transferService,
                        recordProcessingService);

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE));

        final IOException actual =
                assertThrows(
                        IOException.class,
                        () -> service.processResourceTransfer(
                                EXECUTION_ID,
                                request));

        assertEquals(
                expected,
                actual);
    }

    @Test
    void processRecordProcessingShouldPropagateFailure()
            throws IOException {

        final TestStorageReader storageReader =
                new TestStorageReader(
                        "input".getBytes());

        final TestStorageOutputProvider outputProvider =
                new TestStorageOutputProvider();

        final FormatReader formatReader =
                new TestFormatReader(
                        record(0, "first"));

        final FormatWriter formatWriter =
                new TestFormatWriter() {
                    @Override
                    public void write(
                            final Record record)
                            throws IOException {

                        throw new IOException(
                                "Record processing failed");
                    }
                };

        final RecordProcessingService recordProcessingService =
                new RecordProcessingService(
                        storageReader,
                        outputProvider);

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        new TestStorageReader(
                                "unused".getBytes()),
                        new TestStorageWriter());

        final ExecutionProcessingService service =
                new ExecutionProcessingService(
                        transferService,
                        recordProcessingService);

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE),
                        formatReader,
                        formatWriter);

        final IOException exception =
                assertThrows(
                        IOException.class,
                        () -> service.processRecordProcessing(
                                EXECUTION_ID,
                                request));

        assertEquals(
                "Record processing failed",
                exception.getMessage());

        assertTrue(outputProvider.outputClosed());
        assertTrue(storageReader.inputClosed());
    }

    @Test
    void processResourceTransferShouldRejectNullExecutionId() {

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE));

        final ExecutionProcessingService service =
                service();

        assertThrows(
                NullPointerException.class,
                () -> service.processResourceTransfer(
                        null,
                        request));
    }

    @Test
    void processResourceTransferShouldRejectNullRequest() {

        final ExecutionProcessingService service =
                service();

        assertThrows(
                NullPointerException.class,
                () -> service.processResourceTransfer(
                        EXECUTION_ID,
                        null));
    }

    @Test
    void processRecordProcessingShouldRejectNullExecutionId() {

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE),
                        new TestFormatReader(),
                        new TestFormatWriter());

        final ExecutionProcessingService service =
                service();

        assertThrows(
                NullPointerException.class,
                () -> service.processRecordProcessing(
                        null,
                        request));
    }

    @Test
    void processRecordProcessingShouldRejectNullRequest() {

        final ExecutionProcessingService service =
                service();

        assertThrows(
                NullPointerException.class,
                () -> service.processRecordProcessing(
                        EXECUTION_ID,
                        null));
    }

    private ExecutionProcessingService service() {

        return new ExecutionProcessingService(
                new ResourceTransferService(
                        new TestStorageReader(
                                "unused".getBytes()),
                        new TestStorageWriter()),
                new RecordProcessingService(
                        new TestStorageReader(
                                "unused".getBytes()),
                        new TestStorageOutputProvider()));
    }

    private static Resource resource(
            final String name) {

        return new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId(
                        name),
                new ResourceLocation(
                        "file:///" + name));
    }

    private static Record record(
            final long sequence,
            final String value) {

        return new Record(
                sequence,
                List.of(
                        new RecordField(
                                "value",
                                RecordValueType.STRING,
                                value)),
                RecordMetadata.empty());
    }

    private static final class TestStorageReader
            implements StorageReader {

        private final byte[] content;
        private boolean inputClosed;

        private TestStorageReader(
                final byte[] content) {

            this.content = content.clone();
        }

        @Override
        public InputStream open(
                final Resource resource) {

            return new ByteArrayInputStream(
                    content) {

                @Override
                public void close()
                        throws IOException {

                    inputClosed = true;
                    super.close();
                }
            };
        }

        private boolean inputClosed() {
            return inputClosed;
        }
    }

    private static final class TestStorageWriter
            implements StorageWriter {

        private final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        private boolean called;
        private ConflictBehavior conflictBehavior;

        @Override
        public void write(
                final Resource destination,
                final InputStream content,
                final ConflictBehavior conflictBehavior)
                throws IOException {

            called = true;
            this.conflictBehavior =
                    conflictBehavior;

            content.transferTo(output);
        }

        private boolean called() {
            return called;
        }

        private byte[] content() {
            return output.toByteArray();
        }

        private ConflictBehavior conflictBehavior() {
            return conflictBehavior;
        }
    }

    private static final class TestStorageOutputProvider
            implements StorageOutputProvider {

        private final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        private boolean outputCommitted;
        private boolean outputClosed;

        @Override
        public StorageOutput open(
                final Resource destination,
                final DeliveryPolicy deliveryPolicy) {

            return new StorageOutput() {

                private boolean closed;

                @Override
                public OutputStream stream() {

                    if (closed) {
                        throw new IllegalStateException(
                                "Storage output is already closed");
                    }

                    return output;
                }

                @Override
                public void commit() {

                    if (closed) {
                        throw new IllegalStateException(
                                "Storage output is already closed");
                    }

                    outputCommitted = true;
                }

                @Override
                public void close() {

                    if (closed) {
                        return;
                    }

                    closed = true;
                    outputClosed = true;
                }
            };
        }

        private boolean outputCommitted() {
            return outputCommitted;
        }

        private boolean outputClosed() {
            return outputClosed;
        }
    }

    private static class TestFormatReader
            implements FormatReader {

        private final List<Record> records;
        private int index;
        private boolean opened;
        private boolean closed;

        private TestFormatReader(
                final Record... records) {

            this.records = List.of(records);
        }

        @Override
        public void open(
                final InputStream inputStream) {

            opened = true;
        }

        @Override
        public FormatReadResult read() {

            if (index >= records.size()) {
                return FormatReadResult.endOfInput();
            }

            return FormatReadResult.record(
                    records.get(index++));
        }

        @Override
        public void close() {

            closed = true;
        }

        private boolean opened() {
            return opened;
        }

        private boolean closed() {
            return closed;
        }
    }

    private static class TestFormatWriter
            implements FormatWriter {

        private final List<Record> records =
                new ArrayList<>();

        private boolean opened;
        private boolean closed;

        @Override
        public void open(
                final OutputStream outputStream) {

            opened = true;
        }

        @Override
        public void write(
                final Record record)
                throws IOException {

            records.add(record);
        }

        @Override
        public void close() {

            closed = true;
        }

        private List<Record> records() {
            return List.copyOf(records);
        }

        private boolean opened() {
            return opened;
        }

        private boolean closed() {
            return closed;
        }
    }
}
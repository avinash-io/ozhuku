package io.github.avinashio.ozhuku.application.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecordProcessingServiceTest {

    @Test
    void shouldStreamRecordsFromReaderToWriter() throws Exception {
        final FakeStorageReader storageReader = new FakeStorageReader();
        final FakeStorageOutputProvider outputProvider =
                new FakeStorageOutputProvider();

        final Record firstRecord = record(0, "first");
        final Record secondRecord = record(1, "second");

        final FakeFormatReader formatReader =
                new FakeFormatReader(firstRecord, secondRecord);
        final FakeFormatWriter formatWriter =
                new FakeFormatWriter();

        final RecordProcessingService service =
                new RecordProcessingService(
                        storageReader,
                        outputProvider);

        service.process(
                resource("source.csv"),
                resource("destination.csv"),
                new DeliveryPolicy(ConflictBehavior.REPLACE),
                formatReader,
                formatWriter);

        assertEquals(
                List.of(firstRecord, secondRecord),
                formatWriter.records());

        assertTrue(formatReader.opened());
        assertTrue(formatReader.closed());
        assertTrue(formatWriter.opened());
        assertTrue(formatWriter.closed());
        assertTrue(outputProvider.outputClosed());
        assertTrue(storageReader.inputClosed());
    }

    private Record record(
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

    private Resource resource(final String name) {
        return new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId(name),
                new ResourceLocation("file:///" + name));
    }

    private static final class FakeStorageReader
            implements StorageReader {

        private boolean inputClosed;

        @Override
        public InputStream open(final Resource resource) {
            return new ByteArrayInputStream(
                    "input".getBytes(StandardCharsets.UTF_8)) {
                @Override
                public void close() throws IOException {
                    inputClosed = true;
                    super.close();
                }
            };
        }

        private boolean inputClosed() {
            return inputClosed;
        }
    }

    private static final class FakeStorageOutputProvider
            implements StorageOutputProvider {

        private final ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        private boolean outputClosed;

        @Override
        public StorageOutput open(
                final Resource destination,
                final DeliveryPolicy deliveryPolicy) {
            return new StorageOutput() {
                @Override
                public OutputStream stream() {
                    return outputStream;
                }

                @Override
                public void close() {
                    outputClosed = true;
                }
            };
        }

        private boolean outputClosed() {
            return outputClosed;
        }
    }

    private static class FakeFormatReader
            implements FormatReader {

        private final List<Record> records;
        private int index;
        private boolean opened;
        private boolean closed;

        protected FakeFormatReader(final Record... records) {
            this.records = List.of(records);
        }

        @Override
        public void open(final InputStream inputStream) {
            opened = true;
        }

        @Override
        public FormatReadResult read() {
            if (index >= records.size()) {
                return FormatReadResult.endOfInput();
            }

            return FormatReadResult.record(records.get(index++));
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

    private static class FakeFormatWriter
            implements FormatWriter {

        private final List<Record> records = new ArrayList<>();
        private boolean opened;
        private boolean closed;

        @Override
        public void open(final OutputStream outputStream) {
            opened = true;
        }

        @Override
        public void write(final Record record) throws IOException {
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

    @Test
    void shouldFailWhenFormatReaderReportsError() {
        final FakeStorageReader storageReader = new FakeStorageReader();
        final FakeStorageOutputProvider outputProvider =
                new FakeStorageOutputProvider();

        final FakeFormatReader formatReader =
                new FakeFormatReader() {
                    @Override
                    public FormatReadResult read() {
                        return FormatReadResult.error(
                                new IOException("Invalid CSV input"));
                    }
                };

        final FakeFormatWriter formatWriter =
                new FakeFormatWriter();

        final RecordProcessingService service =
                new RecordProcessingService(
                        storageReader,
                        outputProvider);

        final IOException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IOException.class,
                () -> service.process(
                        resource("source.csv"),
                        resource("destination.csv"),
                        new DeliveryPolicy(ConflictBehavior.REPLACE),
                        formatReader,
                        formatWriter));

        assertEquals("Format reader reported an error", exception.getMessage());
        assertTrue(formatReader.closed());
        assertTrue(formatWriter.closed());
        assertTrue(outputProvider.outputClosed());
        assertTrue(storageReader.inputClosed());
    }

    @Test
    void shouldFailWhenFormatWriterFails() {
        final FakeStorageReader storageReader = new FakeStorageReader();
        final FakeStorageOutputProvider outputProvider =
                new FakeStorageOutputProvider();

        final FakeFormatReader formatReader =
                new FakeFormatReader(record(0, "first"));

        final FakeFormatWriter formatWriter =
                new FakeFormatWriter() {
                    @Override
                    public void write(final Record record) throws IOException {
                        throw new IOException("Destination write failed");
                    }
                };

        final RecordProcessingService service =
                new RecordProcessingService(
                        storageReader,
                        outputProvider);

        final IOException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IOException.class,
                () -> service.process(
                        resource("source.csv"),
                        resource("destination.csv"),
                        new DeliveryPolicy(ConflictBehavior.REPLACE),
                        formatReader,
                        formatWriter));

        assertEquals("Destination write failed", exception.getMessage());
        assertTrue(formatReader.closed());
        assertTrue(formatWriter.closed());
        assertTrue(outputProvider.outputClosed());
        assertTrue(storageReader.inputClosed());
    }
}
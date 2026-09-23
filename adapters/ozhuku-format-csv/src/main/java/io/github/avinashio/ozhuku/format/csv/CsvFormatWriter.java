package io.github.avinashio.ozhuku.format.csv;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.format.FormatWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * Writes format-neutral Ozhuku records as CSV.
 */
public final class CsvFormatWriter implements FormatWriter {

    private final CsvWriterConfiguration configuration;

    private BufferedWriter writer;
    private CSVPrinter printer;
    private boolean opened;
    private boolean headerWritten;

    public CsvFormatWriter(
            final CsvWriterConfiguration configuration) {

        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null");
    }

    @Override
    public void open(
            final OutputStream outputStream)
            throws IOException {

        Objects.requireNonNull(
                outputStream,
                "outputStream must not be null");

        if (opened) {
            throw new IllegalStateException(
                    "CSV writer is already open");
        }

        final Charset charset =
                configuration.charset();

        writer = new BufferedWriter(
                new OutputStreamWriter(
                        outputStream,
                        charset));

        final CSVFormat format =
                CSVFormat.DEFAULT.builder()
                        .setDelimiter(
                                configuration.delimiter())
                        .setQuote(
                                configuration.quoteCharacter())
                        .setEscape(
                                configuration.escapeCharacter())
                        .get();

        printer = new CSVPrinter(
                writer,
                format);

        opened = true;
        headerWritten = false;
    }

    @Override
    public void write(
            final Record record)
            throws IOException {

        Objects.requireNonNull(
                record,
                "record must not be null");

        if (!opened) {
            throw new IllegalStateException(
                    "CSV writer must be opened before writing");
        }

        final List<RecordField> fields =
                record.fields();

        if (configuration.writeHeader()
                && !headerWritten) {

            for (final RecordField field : fields) {
                printer.print(field.name());
            }

            printer.println();
            headerWritten = true;
        }

        for (final RecordField field : fields) {
            printer.print(field.value());
        }

        printer.println();
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (printer != null) {
            try {
                printer.flush();
            } catch (final IOException exception) {
                failure = exception;
            }

            try {
                printer.close();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        printer = null;
        writer = null;
        opened = false;
        headerWritten = false;

        if (failure != null) {
            throw failure;
        }
    }
}
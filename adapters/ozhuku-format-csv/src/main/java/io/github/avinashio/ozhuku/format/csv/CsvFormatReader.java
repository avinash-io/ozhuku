package io.github.avinashio.ozhuku.format.csv;

import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Reads CSV input as format-neutral Ozhuku records.
 */
public final class CsvFormatReader implements FormatReader {

    private final CsvReaderConfiguration configuration;

    private BufferedReader reader;
    private CSVParser parser;
    private Iterator<CSVRecord> records;
    private List<String> headers;
    private long sequence;
    private boolean opened;
    private boolean endOfInput;

    public CsvFormatReader(
            final CsvReaderConfiguration configuration) {

        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null");
    }

    @Override
    public void open(
            final InputStream inputStream)
            throws IOException {

        Objects.requireNonNull(
                inputStream,
                "inputStream must not be null");

        if (opened) {
            throw new IllegalStateException(
                    "CSV reader is already open");
        }

        final Charset charset =
                configuration.charset();

        reader = new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        charset));

        parser = CSVFormat.DEFAULT.builder()
                .setDelimiter(
                        configuration.delimiter())
                .setQuote(
                        configuration.quoteCharacter())
                .setEscape(
                        configuration.escapeCharacter())
                .get()
                .parse(reader);

        records = parser.iterator();
        headers = null;
        sequence = 0;
        endOfInput = false;
        opened = true;

        if (configuration.headerPresent()) {
            if (records.hasNext()) {
                final CSVRecord headerRecord =
                        records.next();

                headers = new ArrayList<>(
                        headerRecord.size());

                for (final String header :
                        headerRecord) {

                    headers.add(header);
                }
            } else {
                headers = List.of();
                endOfInput = true;
            }
        }
    }

    @Override
    public FormatReadResult read()
            throws IOException {

        if (!opened) {
            throw new IllegalStateException(
                    "CSV reader must be opened before reading");
        }

        if (endOfInput) {
            return FormatReadResult.endOfInput();
        }

        if (!records.hasNext()) {
            endOfInput = true;
            return FormatReadResult.endOfInput();
        }

        final CSVRecord csvRecord =
                records.next();

        final List<RecordField> fields =
                new ArrayList<>(csvRecord.size());

        for (int index = 0;
             index < csvRecord.size();
             index++) {

            fields.add(
                    new RecordField(
                            resolveFieldName(index),
                            RecordValueType.STRING,
                            csvRecord.get(index)));
        }

        final Record record =
                new Record(
                        sequence++,
                        fields,
                        RecordMetadata.empty());

        return FormatReadResult.record(record);
    }

    private String resolveFieldName(
            final int index) {

        if (headers != null
                && index < headers.size()) {

            final String header =
                    headers.get(index);

            if (!header.isBlank()) {
                return header;
            }
        }

        return "field" + index;
    }

    @Override
    public void close()
            throws IOException {

        IOException failure = null;

        if (parser != null) {
            try {
                parser.close();
            } catch (final IOException exception) {
                failure = exception;
            }
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        parser = null;
        reader = null;
        records = null;
        headers = null;
        opened = false;
        endOfInput = false;
        sequence = 0;

        if (failure != null) {
            throw failure;
        }
    }
}

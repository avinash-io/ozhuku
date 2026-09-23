package io.github.avinashio.ozhuku.format;

import io.github.avinashio.ozhuku.domain.record.Record;
import java.io.IOException;
import java.io.OutputStream;

public interface FormatWriter extends AutoCloseable {

    void open(OutputStream outputStream) throws IOException;

    void write(Record record) throws IOException;

    @Override
    void close() throws IOException;
}
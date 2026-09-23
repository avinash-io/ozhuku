package io.github.avinashio.ozhuku.format;

import io.github.avinashio.ozhuku.domain.record.Record;
import java.io.IOException;

public interface FormatWriter extends AutoCloseable {

    void open() throws IOException;

    void write(Record record) throws IOException;

    @Override
    void close() throws IOException;
}
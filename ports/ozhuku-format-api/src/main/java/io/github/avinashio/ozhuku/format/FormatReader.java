package io.github.avinashio.ozhuku.format;

import io.github.avinashio.ozhuku.domain.record.Record;
import java.io.IOException;

public interface FormatReader extends AutoCloseable {

    void open() throws IOException;

    FormatReadResult read() throws IOException;

    @Override
    void close() throws IOException;
}
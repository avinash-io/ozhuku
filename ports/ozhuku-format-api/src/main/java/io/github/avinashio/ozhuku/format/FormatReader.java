package io.github.avinashio.ozhuku.format;

import java.io.IOException;
import java.io.InputStream;

public interface FormatReader extends AutoCloseable {

    void open(InputStream inputStream) throws IOException;

    FormatReadResult read() throws IOException;

    @Override
    void close() throws IOException;
}
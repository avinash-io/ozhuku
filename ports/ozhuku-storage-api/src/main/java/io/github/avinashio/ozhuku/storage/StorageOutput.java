package io.github.avinashio.ozhuku.storage;

import java.io.IOException;
import java.io.OutputStream;

public interface StorageOutput extends AutoCloseable {

    OutputStream stream();

    void commit() throws IOException;

    @Override
    void close() throws IOException;
}

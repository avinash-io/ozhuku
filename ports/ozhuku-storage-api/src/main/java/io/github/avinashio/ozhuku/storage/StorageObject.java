package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public interface StorageObject {

    Resource resource();

    InputStream openStream() throws IOException;

    long size() throws IOException;

    @Override
    String toString();
}
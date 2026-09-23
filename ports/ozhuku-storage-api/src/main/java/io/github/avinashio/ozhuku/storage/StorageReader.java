package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;
import java.io.InputStream;

public interface StorageReader {

    InputStream open(
            Resource resource) throws IOException;
}
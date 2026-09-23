package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;
import java.io.InputStream;

public interface StorageWriter {

    void write(
            Resource destination,
            InputStream content,
            ConflictBehavior conflictBehavior)
            throws IOException;
}
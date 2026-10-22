package io.github.avinashio.ozhuku.acquisition;

import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;
import java.io.InputStream;

/**
 * Acquires resource content from a source location.
 *
 * <p>A resource acquirer reads resource content from a source for
 * downstream processing. Acquisition is separate from processing:
 * the acquirer provides the byte stream, while format readers,
 * record processors, and writers handle interpretation and delivery.</p>
 *
 * <p>Implementations must stream content to avoid loading entire
 * resources into memory.</p>
 */
public interface ResourceAcquirer {

    /**
     * Opens a streaming input for the given resource.
     *
     * <p>The caller is responsible for closing the returned stream.</p>
     *
     * @param resource the resource to acquire
     * @return an input stream of the resource content
     * @throws IOException if the resource cannot be acquired
     */
    InputStream acquire(
            Resource resource) throws IOException;
}

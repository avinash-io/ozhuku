package io.github.avinashio.ozhuku.acquisition;

import java.io.IOException;
import java.util.List;

/**
 * Scans a source location for available resources.
 *
 * <p>A source scanner discovers resources available for acquisition from a
 * particular source. It performs no resource processing or data transfer
 * beyond what is necessary to enumerate available resources. Scanning is
 * read-only and idempotent.</p>
 *
 * <p>Implementations may perform network or filesystem access. Callers should
 * treat scan results as point-in-time observations.</p>
 */
public interface SourceScanner {

    /**
     * Scans the source and returns the discovered resources.
     *
     * @return list of discovered resources, never {@code null} but may be empty
     * @throws IOException if the source cannot be scanned
     */
    List<ScannedResource> scan() throws IOException;
}

package io.github.avinashio.ozhuku.acquisition.push;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * InputStream wrapper that counts bytes read and feeds data to a
 * {@link MessageDigest} for checksum computation.
 */
final class CountingInputStream extends InputStream {

    private final InputStream delegate;
    private final MessageDigest digest;
    private long count;

    CountingInputStream(
            final InputStream delegate,
            final MessageDigest digest) {

        this.delegate = Objects.requireNonNull(
                delegate,
                "delegate must not be null");
        this.digest = Objects.requireNonNull(
                digest,
                "digest must not be null");
        this.count = 0;
    }

    @Override
    public int read() throws IOException {
        final int b = delegate.read();
        if (b != -1) {
            digest.update((byte) b);
            count++;
        }
        return b;
    }

    @Override
    public int read(
            final byte[] b,
            final int off,
            final int len) throws IOException {

        final int read = delegate.read(b, off, len);
        if (read > 0) {
            digest.update(b, off, read);
            count += read;
        }
        return read;
    }

    long count() {
        return count;
    }
}

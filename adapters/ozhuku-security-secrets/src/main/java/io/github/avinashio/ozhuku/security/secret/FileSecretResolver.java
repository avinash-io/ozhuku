package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Secret resolver that reads secrets from files on the filesystem.
 *
 * <p>Secret references use the scheme {@code file:///absolute/path/to/secret}
 * or {@code file://relative/path}. The file content is read as UTF-8 and
 * returned as a character array. Trailing whitespace (including newlines)
 * is stripped to accommodate files that store secrets with trailing
 * newlines.</p>
 *
 * <p>If the file does not exist or cannot be read, the reference resolves
 * to an empty {@link Optional}.</p>
 */
public final class FileSecretResolver implements SecretResolver {

    private static final String SCHEME = "file://";

    /**
     * Creates a file-based secret resolver.
     */
    public FileSecretResolver() {
        // Default constructor.
    }

    @Override
    public Optional<char[]> resolve(final SecretReference reference) {
        Validation.requireNonNull(
                reference,
                "reference must not be null");

        final String ref = reference.reference();

        if (!ref.startsWith(SCHEME)) {
            return Optional.empty();
        }

        final Path path = Path.of(ref.substring(SCHEME.length()));

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try {
            final byte[] bytes = Files.readAllBytes(path);
            final String content = new String(bytes, StandardCharsets.UTF_8)
                    .trim();

            return Optional.of(content.toCharArray());
        } catch (final IOException exception) {
            return Optional.empty();
        }
    }
}

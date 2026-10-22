package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSecretResolverTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldResolveFileSecret() throws IOException {
        final Path secretFile = tempDir.resolve("password.txt");
        Files.writeString(secretFile, "my-secret-password");

        final FileSecretResolver resolver = new FileSecretResolver();
        final Optional<char[]> result = resolver.resolve(
                new SecretReference(
                        "file://" + secretFile.toAbsolutePath()));

        assertEquals(Optional.of("my-secret-password".toCharArray()).isPresent(),
                result.isPresent());
        assertArrayEquals("my-secret-password".toCharArray(), result.get());
    }

    @Test
    void shouldTrimTrailingWhitespace() throws IOException {
        final Path secretFile = tempDir.resolve("api-key.txt");
        Files.writeString(secretFile, "api-key-123\n\n");

        final FileSecretResolver resolver = new FileSecretResolver();
        final Optional<char[]> result = resolver.resolve(
                new SecretReference(
                        "file://" + secretFile.toAbsolutePath()));

        assertArrayEquals("api-key-123".toCharArray(), result.get());
    }

    @Test
    void shouldReturnEmptyForMissingFile() {
        final FileSecretResolver resolver = new FileSecretResolver();
        final Optional<char[]> result = resolver.resolve(
                new SecretReference(
                        "file:///nonexistent/path/to/secret.txt"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldReturnEmptyForNonFileReference() {
        final FileSecretResolver resolver = new FileSecretResolver();
        final Optional<char[]> result = resolver.resolve(
                new SecretReference("env://MY_VAR"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldHandleNullReference() {
        final FileSecretResolver resolver = new FileSecretResolver();
        assertThrows(
                ValidationException.class,
                () -> resolver.resolve(null));
    }
}

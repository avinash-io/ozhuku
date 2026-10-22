package io.github.avinashio.ozhuku.security.secret;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecretResolverTest {

    @Test
    void shouldResolveSecretFromString() {
        final SecretResolver resolver = new SecretResolver() {
            @Override
            public Optional<char[]> resolve(final SecretReference ref) {
                if ("vault://smtp/password".equals(ref.reference())) {
                    return Optional.of("secret-value".toCharArray());
                }
                return Optional.empty();
            }
        };

        final char[] secret = resolver.require(
                new SecretReference("vault://smtp/password"));

        assertArrayEquals(
                "secret-value".toCharArray(),
                secret);
    }

    @Test
    void shouldResolveAsString() {
        final SecretResolver resolver = new SecretResolver() {
            @Override
            public Optional<char[]> resolve(final SecretReference ref) {
                if ("vault://smtp/user".equals(ref.reference())) {
                    return Optional.of("admin".toCharArray());
                }
                return Optional.empty();
            }
        };

        final Optional<String> value = resolver.resolveAsString(
                new SecretReference("vault://smtp/user"));

        assertEquals(Optional.of("admin"), value);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        final SecretResolver resolver = new SecretResolver() {
            @Override
            public Optional<char[]> resolve(final SecretReference ref) {
                return Optional.empty();
            }
        };

        assertEquals(
                Optional.empty(),
                resolver.resolve(
                        new SecretReference("vault://nonexistent")));
    }

    @Test
    void shouldThrowWhenRequiredAndNotFound() {
        final SecretResolver resolver = new SecretResolver() {
            @Override
            public Optional<char[]> resolve(final SecretReference ref) {
                return Optional.empty();
            }
        };

        assertThrows(
                SecretResolutionException.class,
                () -> resolver.require(
                        new SecretReference("vault://nonexistent")));
    }

    @Test
    void shouldResolveFromMap() {
        final Map<String, String> store = Map.of(
                "vault://smtp/password", "p@ssw0rd");

        final SecretResolver resolver = new SecretResolver() {
            @Override
            public Optional<char[]> resolve(final SecretReference ref) {
                return Optional.ofNullable(store.get(ref.reference()))
                        .map(String::toCharArray);
            }
        };

        final char[] value = resolver.require(
                new SecretReference("vault://smtp/password"));

        assertArrayEquals(
                "p@ssw0rd".toCharArray(),
                value);
    }
}

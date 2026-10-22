package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompositeSecretResolverTest {

    @Test
    void shouldResolveFromFirstResolver() {
        final SecretResolver first = ref ->
                Optional.of("from-first".toCharArray());

        final SecretResolver second = ref ->
                Optional.empty();

        final CompositeSecretResolver composite =
                new CompositeSecretResolver(List.of(first, second));

        final Optional<char[]> result = composite.resolve(
                new SecretReference("vault://secret"));

        assertArrayEquals("from-first".toCharArray(), result.get());
    }

    @Test
    void shouldResolveFromSecondResolver() {
        final SecretResolver first = ref ->
                Optional.empty();

        final SecretResolver second = ref ->
                Optional.of("from-second".toCharArray());

        final CompositeSecretResolver composite =
                new CompositeSecretResolver(List.of(first, second));

        final Optional<char[]> result = composite.resolve(
                new SecretReference("vault://secret"));

        assertArrayEquals("from-second".toCharArray(), result.get());
    }

    @Test
    void shouldReturnEmptyWhenNoResolverMatches() {
        final SecretResolver first = ref ->
                Optional.empty();

        final SecretResolver second = ref ->
                Optional.empty();

        final CompositeSecretResolver composite =
                new CompositeSecretResolver(List.of(first, second));

        final Optional<char[]> result = composite.resolve(
                new SecretReference("vault://missing"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldRejectNullResolvers() {
        assertThrows(
                ValidationException.class,
                () -> new CompositeSecretResolver(null));
    }
}

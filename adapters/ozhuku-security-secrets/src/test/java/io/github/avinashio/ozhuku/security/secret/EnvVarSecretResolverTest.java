package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvVarSecretResolverTest {

    @Test
    void shouldReturnEmptyForNonEnvReference() {
        final EnvVarSecretResolver resolver = new EnvVarSecretResolver();

        final Optional<char[]> result = resolver.resolve(
                new SecretReference("vault://secret/key"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldHandleNullReference() {
        final EnvVarSecretResolver resolver = new EnvVarSecretResolver();

        assertThrows(
                ValidationException.class,
                () -> resolver.resolve(null));
    }

    @Test
    void shouldHandleEmptyEnvVarName() {
        final EnvVarSecretResolver resolver = new EnvVarSecretResolver();

        final Optional<char[]> result = resolver.resolve(
                new SecretReference("env://"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldReturnEmptyForUnsetEnvVar() {
        final EnvVarSecretResolver resolver = new EnvVarSecretResolver();

        final Optional<char[]> result = resolver.resolve(
                new SecretReference("env://DEFINITELY_NOT_SET_VAR_12345"));

        assertEquals(Optional.empty(), result);
    }
}

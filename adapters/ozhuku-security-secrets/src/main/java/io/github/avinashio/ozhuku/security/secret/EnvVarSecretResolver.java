package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Optional;

/**
 * Secret resolver that reads secrets from environment variables.
 *
 * <p>Secret references use the scheme {@code env://VARIABLE_NAME}. For
 * example, the reference {@code env://SMTP_PASSWORD} resolves to the
 * value of the environment variable {@code SMTP_PASSWORD}.</p>
 *
 * <p>If the environment variable is not set, the reference resolves to
 * an empty {@link Optional}.</p>
 */
public final class EnvVarSecretResolver implements SecretResolver {

    private static final String SCHEME = "env://";

    /**
     * Creates an environment-variable secret resolver.
     */
    public EnvVarSecretResolver() {
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

        final String envVar = ref.substring(SCHEME.length());

        if (envVar.isEmpty()) {
            return Optional.empty();
        }

        final String value = System.getenv(envVar);

        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(value.toCharArray());
    }
}

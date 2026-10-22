package io.github.avinashio.ozhuku.security.secret;

import java.util.Optional;

/**
 * Resolves {@link SecretReference} instances to secret values.
 *
 * <p>This interface abstracts the source of secrets (environment variables,
 * files, vault services, etc.) so that the core domain and adapters can
 * obtain secrets without depending on any specific secret-management
 * technology. Implementations live in adapter modules.</p>
 *
 * <p>Security note: {@link #resolve(SecretReference)} returns an
 * {@link Optional}. A missing secret resolves to {@link Optional#empty()},
 * which allows the caller to distinguish between "no value configured"
 * and "an error occurred during resolution".</p>
 */
public interface SecretResolver {

    /**
     * Resolves a secret reference to its value.
     *
     * @param reference the secret reference
     * @return the resolved secret as an {@link Optional}, empty if not found
     */
    Optional<char[]> resolve(SecretReference reference);

    /**
     * Resolves a secret reference to its value as a UTF-8 string.
     *
     * @param reference the secret reference
     * @return the resolved secret as an {@link Optional}, empty if not found
     */
    default Optional<String> resolveAsString(SecretReference reference) {
        return resolve(reference)
                .map(String::new);
    }

    /**
     * Resolves a secret reference, requiring that the secret exists.
     *
     * @param reference the secret reference
     * @return the resolved secret bytes
     * @throws SecretResolutionException if the secret cannot be resolved
     */
    default char[] require(SecretReference reference) {
        return resolve(reference)
                .orElseThrow(() -> new SecretResolutionException(
                        "Secret not found: " + reference.reference()));
    }
}

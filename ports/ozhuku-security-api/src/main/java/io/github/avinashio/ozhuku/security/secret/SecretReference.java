package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.validation.Validation;

/**
 * A reference to a secret stored in an external secret store.
 *
 * <p>This is a value object that identifies a secret by a logical key
 * (e.g. {@code "mail/operations-smtp-password"}) without revealing
 * the secret's actual value. The reference is resolved at runtime by
 * a {@link SecretResolver} to obtain the secret bytes.</p>
 *
 * <p>Pipeline configurations and connection profiles store
 * {@code SecretReference} values rather than literal secret values,
 * ensuring that credentials never appear in source-controlled
 * YAML definitions.</p>
 */
public final class SecretReference {

    private final String reference;

    /**
     * Creates a secret reference from a reference string.
     *
     * @param reference the secret reference key (e.g. "vault://smtp/password")
     */
    public SecretReference(final String reference) {
        this.reference = Validation.requireNonBlank(
                reference,
                "Secret reference must not be blank");
    }

    /**
     * Returns the reference key.
     *
     * @return the secret reference key
     */
    public String reference() {
        return reference;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SecretReference)) {
            return false;
        }

        final SecretReference that = (SecretReference) other;
        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return reference.hashCode();
    }

    @Override
    public String toString() {
        return reference;
    }
}

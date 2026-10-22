package io.github.avinashio.ozhuku.security.secret;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.List;
import java.util.Optional;

/**
 * Secret resolver that delegates to a list of other resolvers in order.
 *
 * <p>The first resolver that returns a non-empty value wins. If no
 * resolver can resolve the reference, the composite resolver returns
 * an empty {@link Optional}.</p>
 */
public final class CompositeSecretResolver implements SecretResolver {

    private final List<SecretResolver> resolvers;

    /**
     * Creates a composite resolver from a list of resolvers.
     *
     * @param resolvers the resolvers to try, in priority order
     */
    public CompositeSecretResolver(final List<SecretResolver> resolvers) {
        this.resolvers = List.copyOf(
                Validation.requireNonNull(
                        resolvers,
                        "resolvers must not be null"));
    }

    @Override
    public Optional<char[]> resolve(final SecretReference reference) {
        for (final SecretResolver resolver : resolvers) {
            final Optional<char[]> value = resolver.resolve(reference);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }
}

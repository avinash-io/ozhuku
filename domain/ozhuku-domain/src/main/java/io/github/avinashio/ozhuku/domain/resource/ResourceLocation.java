package io.github.avinashio.ozhuku.domain.resource;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.net.URI;
import java.util.Objects;

/**
 * Identifies the logical location of a resource.
 *
 * <p>The location describes where a resource is located. It does not
 * provide access to the resource or contain connection credentials.</p>
 */
public final class ResourceLocation {

    private final URI uri;

    /**
     * Creates a resource location from a URI.
     *
     * @param value resource location URI
     */
    public ResourceLocation(final String value) {
        final String validatedValue = Validation.requireNonBlank(
                value,
                "Resource location must not be blank");

        try {
            this.uri = URI.create(validatedValue);
        } catch (final IllegalArgumentException exception) {
            throw new ValidationException(
                    "Resource location must be a valid URI",
                    exception);
        }

        Validation.requireNonBlank(
                uri.getScheme(),
                "Resource location must contain a scheme");
    }

    /**
     * Returns the URI representation.
     *
     * @return resource location URI
     */
    public URI uri() {
        return uri;
    }

    /**
     * Returns the location scheme.
     *
     * @return scheme such as file or s3
     */
    public String scheme() {
        return uri.getScheme();
    }

    /**
     * Returns the normalized textual location.
     *
     * @return location value
     */
    public String value() {
        return uri.toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ResourceLocation)) {
            return false;
        }

        final ResourceLocation that = (ResourceLocation) other;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
package io.github.avinashio.ozhuku.server.diagnostics;

import java.util.Objects;

/**
 * Result of a targeted dependency health check.
 */
public record DependencyHealth(
        String scope,
        String status,
        String detail) {

    /**
     * Creates a healthy dependency health result.
     *
     * @param scope  the dependency scope
     * @param detail additional detail
     * @return a healthy result
     */
    public static DependencyHealth healthy(
            final String scope,
            final String detail) {

        return new DependencyHealth(scope, "HEALTHY", detail);
    }

    /**
     * Creates an unhealthy dependency health result.
     *
     * @param scope  the dependency scope
     * @param detail additional detail
     * @return an unhealthy result
     */
    public static DependencyHealth unhealthy(
            final String scope,
            final String detail) {

        return new DependencyHealth(scope, "UNHEALTHY", detail);
    }
}

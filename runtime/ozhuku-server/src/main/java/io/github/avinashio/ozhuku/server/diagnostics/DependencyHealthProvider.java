package io.github.avinashio.ozhuku.server.diagnostics;

/**
 * Provides targeted health checks for a specific dependency scope.
 *
 * <p>Implementations check a specific category of dependencies (e.g.
 * database, storage, mail) and return a {@link DependencyHealth}
 * result without affecting liveness or readiness.</p>
 */
public interface DependencyHealthProvider {

    /**
     * Performs a health check for this dependency scope.
     *
     * @return the health result
     */
    DependencyHealth check();
}

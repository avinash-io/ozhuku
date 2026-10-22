package io.github.avinashio.ozhuku.server.diagnostics;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operator-triggered diagnostics endpoints.
 *
 * <p>Provides targeted, non-automatic dependency health checks that
 * operators can invoke on demand. These endpoints do not affect
 * liveness or readiness.</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /api/v1/operations/health/dependencies} - list
 *       all configured dependency groups</li>
 *   <li>{@code GET /api/v1/operations/health/dependencies/{scope} -
 *       check specific dependency group</li>
 * </ul></p>
 */
@RestController
public class DiagnosticsController {

    private final Map<String, DependencyHealthProvider> providers;

    /**
     * Creates a diagnostics controller.
     *
     * @param providers map of dependency scope to health provider
     */
    public DiagnosticsController(
            final Map<String, DependencyHealthProvider> providers) {
        this.providers = Map.copyOf(providers);
    }

    /**
     * Lists all available dependency scopes.
     *
     * @return list of dependency scope names
     */
    @GetMapping(path = "/api/v1/operations/health/dependencies",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> listDependencies() {
        return List.copyOf(providers.keySet());
    }

    /**
     * Checks the health of a specific dependency scope.
     *
     * @param scope the dependency scope (e.g. DATABASE, STORAGE, MAIL)
     * @return health result for the scope
     */
    @GetMapping(path = "/api/v1/operations/health/dependencies/{scope}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DependencyHealth checkDependency(
            @PathVariable final String scope) {

        final DependencyHealthProvider provider = providers.get(scope);
        if (provider == null) {
            return new DependencyHealth(scope, "UNKNOWN",
                    "Scope not configured");
        }
        return provider.check();
    }
}

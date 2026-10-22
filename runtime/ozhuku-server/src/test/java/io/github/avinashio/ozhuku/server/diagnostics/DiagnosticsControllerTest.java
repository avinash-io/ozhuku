package io.github.avinashio.ozhuku.server.diagnostics;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiagnosticsControllerTest {

    @Test
    void shouldListAllDependencyScopes() {
        final DependencyHealthProvider databaseProvider =
                mock(DependencyHealthProvider.class);
        when(databaseProvider.check()).thenReturn(
                DependencyHealth.healthy("DATABASE", "ok"));

        final DiagnosticsController controller =
                new DiagnosticsController(
                        Map.of("DATABASE", databaseProvider));

        final List<String> scopes = controller.listDependencies();

        assertEquals(1, scopes.size());
        assertTrue(scopes.contains("DATABASE"));
    }

    @Test
    void shouldReturnHealthForKnownScope() {
        final DependencyHealthProvider databaseProvider =
                mock(DependencyHealthProvider.class);
        when(databaseProvider.check()).thenReturn(
                DependencyHealth.healthy("DATABASE", "ok"));

        final DiagnosticsController controller =
                new DiagnosticsController(
                        Map.of("DATABASE", databaseProvider));

        final DependencyHealth health =
                controller.checkDependency("DATABASE");

        assertEquals("DATABASE", health.scope());
        assertEquals("HEALTHY", health.status());
    }

    @Test
    void shouldReturnUnknownForUnconfiguredScope() {
        final DependencyHealthProvider databaseProvider =
                mock(DependencyHealthProvider.class);
        when(databaseProvider.check()).thenReturn(
                DependencyHealth.healthy("DATABASE", "ok"));

        final DiagnosticsController controller =
                new DiagnosticsController(
                        Map.of("DATABASE", databaseProvider));

        final DependencyHealth health =
                controller.checkDependency("STORAGE");

        assertEquals("STORAGE", health.scope());
        assertEquals("UNKNOWN", health.status());
        assertEquals("Scope not configured", health.detail());
    }

    @Test
    void shouldHandleEmptyProviderMap() {
        final DiagnosticsController controller =
                new DiagnosticsController(Map.of());

        assertTrue(controller.listDependencies().isEmpty());

        final DependencyHealth health =
                controller.checkDependency("DATABASE");

        assertEquals("UNKNOWN", health.status());
    }
}

package io.github.avinashio.ozhuku.server.config;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OzhukuServerPropertiesTest {

    @Test
    void shouldApplyDefaultValuesOnConstruction() {
        final OzhukuServerProperties properties =
                new OzhukuServerProperties();

        assertEquals("./workspace", properties.storageRoot());
        assertEquals(4, properties.getMaxConcurrentExecutions());
        assertEquals(false, properties.isDistributedEnabled());
        assertEquals(30, properties.getLeaseTimeoutSeconds());
    }

    @Test
    void shouldStoreAndReturnStorageRoot() {
        final OzhukuServerProperties properties =
                new OzhukuServerProperties();
        properties.setStorageRoot("./data");

        assertEquals("./data", properties.storageRoot());
        assertEquals(Path.of("./data"),
                properties.storageRootPath());
    }

    @Test
    void shouldEnforceMinimumConcurrentExecutions() {
        final OzhukuServerProperties properties =
                new OzhukuServerProperties();
        properties.setMaxConcurrentExecutions(0);

        assertEquals(1, properties.getMaxConcurrentExecutions());
    }

    @Test
    void shouldAllowCustomConcurrentExecutions() {
        final OzhukuServerProperties properties =
                new OzhukuServerProperties();
        properties.setMaxConcurrentExecutions(8);

        assertEquals(8, properties.getMaxConcurrentExecutions());
    }

    @Test
    void shouldAllowCustomLeaseTimeout() {
        final OzhukuServerProperties properties =
                new OzhukuServerProperties();
        properties.setLeaseTimeoutSeconds(60);

        assertEquals(60, properties.getLeaseTimeoutSeconds());
    }

    @Test
    void shouldEnableDistributedMode() {
        final OzhukuServerProperties properties =
                new OzhukuServerProperties();
        properties.setDistributedEnabled(true);

        assertEquals(true, properties.isDistributedEnabled());
    }
}

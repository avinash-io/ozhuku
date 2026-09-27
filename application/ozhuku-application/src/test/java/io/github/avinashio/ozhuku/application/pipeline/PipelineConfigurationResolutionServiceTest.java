package io.github.avinashio.ozhuku.application.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineConfiguration;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineDefinition;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.persistence.PipelineConfigurationRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PipelineConfigurationResolutionServiceTest {

    private InMemoryPipelineConfigurationRepository repository;
    private PipelineConfigurationResolutionService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPipelineConfigurationRepository();

        service =
                new PipelineConfigurationResolutionService(
                        repository,
                        new PipelinePlanResolver());
    }

    @Test
    void shouldResolvePersistedConfigurationIntoPipelinePlan() {
        final PipelineConfiguration configuration =
                createConfiguration(
                        "pipeline-resolve",
                        1,
                        "Resolvable configuration");

        repository.save(configuration);

        final PipelinePlan result =
                service.resolve(
                        new PipelineId("pipeline-resolve"),
                        new PipelineVersion(1));

        assertEquals(
                configuration.pipelineDefinition(),
                result.pipelineDefinition());
        assertEquals(configuration.flow(), result.flow());
        assertEquals(configuration.source(), result.source());
        assertEquals(configuration.destination(), result.destination());
        assertEquals(
                configuration.deliveryPolicy(),
                result.deliveryPolicy());
    }

    @Test
    void shouldResolveRequestedPipelineVersion() {
        final PipelineConfiguration versionOne =
                createConfiguration(
                        "pipeline-versioned",
                        1,
                        "Version one");

        final PipelineConfiguration versionTwo =
                createConfiguration(
                        "pipeline-versioned",
                        2,
                        "Version two");

        repository.save(versionOne);
        repository.save(versionTwo);

        final PipelinePlan result =
                service.resolve(
                        new PipelineId("pipeline-versioned"),
                        new PipelineVersion(2));

        assertEquals(
                versionTwo.pipelineDefinition(),
                result.pipelineDefinition());
    }

    @Test
    void shouldRejectMissingPipelineConfiguration() {
        final IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.resolve(
                                        new PipelineId("pipeline-missing"),
                                        new PipelineVersion(1)));

        assertEquals(
                "Pipeline configuration not found: pipeline-missing:1",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullPipelineId() {
        assertThrows(
                RuntimeException.class,
                () ->
                        service.resolve(
                                null,
                                new PipelineVersion(1)));
    }

    @Test
    void shouldRejectNullPipelineVersion() {
        assertThrows(
                RuntimeException.class,
                () ->
                        service.resolve(
                                new PipelineId("pipeline-null-version"),
                                null));
    }

    private static PipelineConfiguration createConfiguration(
            final String pipelineId,
            final long version,
            final String description) {

        final PipelineDefinition pipelineDefinition =
                new PipelineDefinition(
                        new PipelineId(pipelineId),
                        new PipelineVersion(version),
                        description);

        final Flow flow =
                new Flow(
                        new FlowId("flow-" + pipelineId + "-" + version),
                        "Test Flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        new ResourceId(
                                "source-" + pipelineId + "-" + version),
                        new ResourceLocation(
                                "file:///input/" + pipelineId + "/" + version));

        final Resource destination =
                new Resource(
                        new ResourceId(
                                "destination-" + pipelineId + "-" + version),
                        new ResourceLocation(
                                "file:///output/" + pipelineId + "/" + version));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(ConflictBehavior.REPLACE);

        return new PipelineConfiguration(
                pipelineDefinition,
                flow,
                source,
                destination,
                deliveryPolicy);
    }

    private static final class InMemoryPipelineConfigurationRepository
            implements PipelineConfigurationRepository {

        private final Map<String, PipelineConfiguration> configurations =
                new HashMap<>();

        @Override
        public Optional<PipelineConfiguration> findByVersion(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {

            return Optional.ofNullable(
                    configurations.get(
                            key(pipelineId, pipelineVersion)));
        }

        @Override
        public void save(final PipelineConfiguration configuration) {
            configurations.put(
                    key(
                            configuration.pipelineDefinition().pipelineId(),
                            configuration.pipelineDefinition().version()),
                    configuration);
        }

        private static String key(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {
            return pipelineId.value() + ":" + pipelineVersion.value();
        }
    }
}
package io.github.avinashio.ozhuku.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PipelineConfigurationRepositoryTest {

    @Test
    void shouldSaveAndFindConfigurationByPipelineAndVersion() {
        final PipelineConfigurationRepository repository =
                new InMemoryPipelineConfigurationRepository();

        final PipelineConfiguration configuration =
                configuration("pipeline-1", 1);

        repository.save(configuration);

        final Optional<PipelineConfiguration> result =
                repository.findByVersion(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1));

        assertTrue(result.isPresent());
        assertEquals(configuration, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenConfigurationDoesNotExist() {
        final PipelineConfigurationRepository repository =
                new InMemoryPipelineConfigurationRepository();

        final Optional<PipelineConfiguration> result =
                repository.findByVersion(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1));

        assertFalse(result.isPresent());
    }

    @Test
    void shouldKeepDifferentPipelineVersionsSeparate() {
        final PipelineConfigurationRepository repository =
                new InMemoryPipelineConfigurationRepository();

        final PipelineConfiguration versionOne =
                configuration("pipeline-1", 1);

        final PipelineConfiguration versionTwo =
                configuration("pipeline-1", 2);

        repository.save(versionOne);
        repository.save(versionTwo);

        assertEquals(
                versionOne,
                repository.findByVersion(
                                new PipelineId("pipeline-1"),
                                new PipelineVersion(1))
                        .orElseThrow());

        assertEquals(
                versionTwo,
                repository.findByVersion(
                                new PipelineId("pipeline-1"),
                                new PipelineVersion(2))
                        .orElseThrow());
    }

    @Test
    void shouldKeepDifferentPipelinesSeparate() {
        final PipelineConfigurationRepository repository =
                new InMemoryPipelineConfigurationRepository();

        final PipelineConfiguration firstPipeline =
                configuration("pipeline-1", 1);

        final PipelineConfiguration secondPipeline =
                configuration("pipeline-2", 1);

        repository.save(firstPipeline);
        repository.save(secondPipeline);

        assertEquals(
                firstPipeline,
                repository.findByVersion(
                                new PipelineId("pipeline-1"),
                                new PipelineVersion(1))
                        .orElseThrow());

        assertEquals(
                secondPipeline,
                repository.findByVersion(
                                new PipelineId("pipeline-2"),
                                new PipelineVersion(1))
                        .orElseThrow());
    }

    private PipelineConfiguration configuration(
            final String pipelineId,
            final long version) {

        return new PipelineConfiguration(
                new PipelineDefinition(
                        new PipelineId(pipelineId),
                        new PipelineVersion(version),
                        "Test pipeline"),
                new Flow(
                        new FlowId("flow-" + pipelineId + "-" + version),
                        "Test flow",
                        FlowMode.RESOURCE_TRANSFER),
                new Resource(
                        new ResourceId("source-" + pipelineId + "-" + version),
                        new ResourceLocation("file:///source")),
                new Resource(
                        new ResourceId("destination-" + pipelineId + "-" + version),
                        new ResourceLocation("file:///destination")),
                new DeliveryPolicy(ConflictBehavior.REPLACE));
    }

    private static final class InMemoryPipelineConfigurationRepository
            implements PipelineConfigurationRepository {

        private final Map<String, PipelineConfiguration> configurations =
                new HashMap<>();

        @Override
        public Optional<PipelineConfiguration> findByVersion(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {

            final String key = key(pipelineId, pipelineVersion);
            return Optional.ofNullable(configurations.get(key));
        }

        @Override
        public void save(final PipelineConfiguration configuration) {
            final PipelineDefinition definition =
                    configuration.pipelineDefinition();

            configurations.put(
                    key(definition.pipelineId(), definition.version()),
                    configuration);
        }

        private String key(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {

            return pipelineId.value() + ":" + pipelineVersion.value();
        }
    }
}
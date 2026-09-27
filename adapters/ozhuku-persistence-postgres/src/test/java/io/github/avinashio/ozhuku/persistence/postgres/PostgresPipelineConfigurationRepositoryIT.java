package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresPipelineConfigurationRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static PostgresPipelineConfigurationRepository repository;

    @BeforeAll
    static void setUp() {
        final DataSource dataSource = createDataSource();

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        repository = new PostgresPipelineConfigurationRepository(dataSource);
    }

    @BeforeEach
    void verifyRepositoryIsAvailable() {
        assertTrue(POSTGRES.isRunning());
    }

    @Test
    void shouldReturnEmptyWhenConfigurationDoesNotExist() {
        final Optional<PipelineConfiguration> result =
                repository.findByVersion(
                        new PipelineId("pipeline-missing"),
                        new PipelineVersion(1));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrieveConfiguration() {
        final PipelineConfiguration configuration =
                createConfiguration(
                        "pipeline-save",
                        1,
                        "Initial pipeline configuration");

        repository.save(configuration);

        final Optional<PipelineConfiguration> result =
                repository.findByVersion(
                        new PipelineId("pipeline-save"),
                        new PipelineVersion(1));

        assertTrue(result.isPresent());
        assertEquals(configuration, result.orElseThrow());
    }

    @Test
    void shouldPreserveCompleteConfiguration() {
        final PipelineConfiguration configuration =
                createConfiguration(
                        "pipeline-complete",
                        1,
                        "Complete configuration");

        repository.save(configuration);

        final PipelineConfiguration result =
                repository.findByVersion(
                                new PipelineId("pipeline-complete"),
                                new PipelineVersion(1))
                        .orElseThrow();

        assertEquals(
                "pipeline-complete",
                result.pipelineDefinition().pipelineId().value());
        assertEquals(
                1,
                result.pipelineDefinition().version().value());
        assertEquals(
                "Complete configuration",
                result.pipelineDefinition().description());

        assertEquals(
                "flow-pipeline-complete",
                result.flow().id().value());
        assertEquals(
                "Complete Flow",
                result.flow().name());
        assertEquals(
                FlowMode.RESOURCE_TRANSFER,
                result.flow().mode());

        assertEquals(
                "source-pipeline-complete",
                result.source().id().value());
        assertEquals(
                "file:///input/source.csv",
                result.source().location().value());

        assertEquals(
                "destination-pipeline-complete",
                result.destination().id().value());
        assertEquals(
                "file:///output/destination.csv",
                result.destination().location().value());

        assertEquals(
                ConflictBehavior.REPLACE,
                result.deliveryPolicy().conflictBehavior());
    }

    @Test
    void shouldKeepDifferentVersionsSeparate() {
        final PipelineConfiguration versionOne =
                createConfiguration(
                        "pipeline-versions",
                        1,
                        "Version one");

        final PipelineConfiguration versionTwo =
                createConfiguration(
                        "pipeline-versions",
                        2,
                        "Version two");

        repository.save(versionOne);
        repository.save(versionTwo);

        assertEquals(
                versionOne,
                repository.findByVersion(
                                new PipelineId("pipeline-versions"),
                                new PipelineVersion(1))
                        .orElseThrow());

        assertEquals(
                versionTwo,
                repository.findByVersion(
                                new PipelineId("pipeline-versions"),
                                new PipelineVersion(2))
                        .orElseThrow());
    }

    @Test
    void shouldKeepDifferentPipelinesSeparate() {
        final PipelineConfiguration firstPipeline =
                createConfiguration(
                        "pipeline-first",
                        1,
                        "First pipeline");

        final PipelineConfiguration secondPipeline =
                createConfiguration(
                        "pipeline-second",
                        1,
                        "Second pipeline");

        repository.save(firstPipeline);
        repository.save(secondPipeline);

        assertEquals(
                firstPipeline,
                repository.findByVersion(
                                new PipelineId("pipeline-first"),
                                new PipelineVersion(1))
                        .orElseThrow());

        assertEquals(
                secondPipeline,
                repository.findByVersion(
                                new PipelineId("pipeline-second"),
                                new PipelineVersion(1))
                        .orElseThrow());
    }

    @Test
    void shouldRejectDuplicatePipelineVersion() {
        final PipelineConfiguration configuration =
                createConfiguration(
                        "pipeline-immutable",
                        1,
                        "Original configuration");

        repository.save(configuration);

        assertThrows(
                IllegalStateException.class,
                () ->
                        repository.save(
                                createConfiguration(
                                        "pipeline-immutable",
                                        1,
                                        "Replacement configuration")));

        final PipelineConfiguration persisted =
                repository.findByVersion(
                                new PipelineId("pipeline-immutable"),
                                new PipelineVersion(1))
                        .orElseThrow();

        assertEquals(
                "Original configuration",
                persisted.pipelineDefinition().description());
    }

    private static DataSource createDataSource() {
        final PGSimpleDataSource dataSource =
                new PGSimpleDataSource();

        dataSource.setURL(POSTGRES.getJdbcUrl());
        dataSource.setUser(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());

        return dataSource;
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
                        new FlowId("flow-" + pipelineId),
                        "Complete Flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        new ResourceId("source-" + pipelineId),
                        new ResourceLocation(
                                "file:///input/source.csv"));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-" + pipelineId),
                        new ResourceLocation(
                                "file:///output/destination.csv"));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE);

        return new PipelineConfiguration(
                pipelineDefinition,
                flow,
                source,
                destination,
                deliveryPolicy);
    }
}

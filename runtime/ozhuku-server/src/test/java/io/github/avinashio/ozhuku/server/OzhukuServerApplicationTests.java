package io.github.avinashio.ozhuku.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.application.pipeline.ConfiguredPipelineExecutionService;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineConfiguration;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineDefinition;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.PipelineConfigurationRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class OzhukuServerApplicationTests {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static Path storageRoot;

    @Autowired
    private ConfiguredPipelineExecutionService configuredPipelineExecutionService;

    @Autowired
    private PipelineConfigurationRepository pipelineConfigurationRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @BeforeAll
    static void setUpStorageRoot() throws IOException {
        storageRoot =
                Files.createTempDirectory(
                        "ozhuku-runtime-test-");
    }

    @DynamicPropertySource
    static void registerProperties(
            final DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl);

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername);

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword);

        registry.add(
                "spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName);

        registry.add(
                "ozhuku.storage.root",
                () -> storageRoot.toString());
    }

    @Test
    void contextLoads() {
        assertTrue(
                POSTGRES.isRunning());
        assertTrue(
                configuredPipelineExecutionService != null);
        assertTrue(
                pipelineConfigurationRepository != null);
        assertTrue(
                executionRepository != null);
    }

    @Test
    void shouldExecutePersistedPipelineConfiguration()
            throws IOException {

        final Path sourcePath =
                storageRoot
                        .resolve("input")
                        .resolve("source.txt");

        Files.createDirectories(
                sourcePath.getParent());

        Files.writeString(
                sourcePath,
                "Ozhuku runtime integration test");

        final PipelineId pipelineId =
                new PipelineId("runtime-pipeline");

        final PipelineVersion pipelineVersion =
                new PipelineVersion(1L);

        final ExecutionId executionId =
                new ExecutionId("runtime-execution");

        final Flow flow =
                new Flow(
                        new FlowId("runtime-flow"),
                        "Runtime Flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        new ResourceId("runtime-source"),
                        new ResourceLocation(
                                "file:///input/source.txt"));

        final Resource destination =
                new Resource(
                        new ResourceId("runtime-destination"),
                        new ResourceLocation(
                                "file:///output/destination.txt"));

        final PipelineConfiguration configuration =
                new PipelineConfiguration(
                        new PipelineDefinition(
                                pipelineId,
                                pipelineVersion,
                                "Runtime configured pipeline"),
                        flow,
                        source,
                        destination,
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE));

        pipelineConfigurationRepository.save(
                configuration);

        final ExecutionReference executionReference =
                new ExecutionReference(
                        executionId,
                        pipelineId,
                        pipelineVersion);

        configuredPipelineExecutionService.execute(
                executionReference,
                pipelineId,
                pipelineVersion);

        final Execution execution =
                executionRepository
                        .findById(executionId)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.COMPLETED,
                execution.status());

        final Path destinationPath =
                storageRoot
                        .resolve("output")
                        .resolve("destination.txt");

        assertTrue(
                Files.exists(destinationPath));

        assertEquals(
                "Ozhuku runtime integration test",
                Files.readString(destinationPath));
    }
}
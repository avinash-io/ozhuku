package io.github.avinashio.ozhuku.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.application.pipeline.ConfiguredPipelineExecutionService;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryExecutor;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryUseCase;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.application.recovery.RecoveryDecision;
import io.github.avinashio.ozhuku.application.recovery.RecoveryReason;
import io.github.avinashio.ozhuku.application.recovery.RecoveryResult;
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
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.PipelineConfigurationRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
    private ConfiguredPipelineExecutionService
            configuredPipelineExecutionService;

    @Autowired
    private PipelineConfigurationRepository
            pipelineConfigurationRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private DestinationRecoveryUseCase
            destinationRecoveryUseCase;

    @Autowired
    private DestinationRecoveryExecutor
            destinationRecoveryExecutor;

    @Autowired
    private DestinationExecutionRepository
            destinationExecutionRepository;

    @Autowired
    private DestinationCommitRepository
            destinationCommitRepository;

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

        assertTrue(
                destinationRecoveryUseCase != null);

        assertTrue(
                destinationRecoveryExecutor != null);
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

    @Test
    void shouldRecoverDestinationWhenCommitIsNotConfirmed() {

        final ExecutionId executionId =
                new ExecutionId(
                        "recovery-execution-not-committed");

        final ResourceId resourceId =
                new ResourceId(
                        "recovery-destination-not-committed");

        final DestinationExecutionReference reference =
                new DestinationExecutionReference(
                        executionId,
                        resourceId);

        final DestinationExecution destinationExecution =
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-28T00:00:00Z"),
                        Instant.parse(
                                "2026-09-28T00:00:10Z"));

        destinationExecutionRepository.save(
                destinationExecution);

        final DestinationCommitReference commitReference =
                new DestinationCommitReference(reference);

        destinationCommitRepository.save(
                DestinationCommit.notCommitted(
                        commitReference));

        final RecoveryResult result =
                destinationRecoveryUseCase.execute(
                        reference);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_NOT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldRemainUnresolvedWhenUnknownCommitCannotBeConfirmed() {

        final ExecutionId executionId =
                new ExecutionId(
                        "recovery-execution-unknown");

        final ResourceId resourceId =
                new ResourceId(
                        "recovery-destination-unknown");

        final DestinationExecutionReference reference =
                new DestinationExecutionReference(
                        executionId,
                        resourceId);

        final DestinationExecution destinationExecution =
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-28T00:01:00Z"),
                        Instant.parse(
                                "2026-09-28T00:01:10Z"));

        destinationExecutionRepository.save(
                destinationExecution);

        final DestinationCommitReference commitReference =
                new DestinationCommitReference(reference);

        destinationCommitRepository.save(
                DestinationCommit.unknown(
                        commitReference));

        final RecoveryResult result =
                destinationRecoveryUseCase.execute(
                        reference);

        assertEquals(
                RecoveryDecision.UNRESOLVED,
                result.decision());

        assertEquals(
                RecoveryReason.DESTINATION_OUTCOME_UNKNOWN,
                result.reason());
    }

    @Test
    void shouldExecuteDestinationRecoveryAndPersistCommit()
            throws IOException {

        final Path sourcePath =
                storageRoot
                        .resolve("recovery")
                        .resolve("input")
                        .resolve("source.txt");

        Files.createDirectories(
                sourcePath.getParent());

        Files.writeString(
                sourcePath,
                "Ozhuku destination recovery integration test");

        final PipelineId pipelineId =
                new PipelineId(
                        "recovery-runtime-pipeline");

        final PipelineVersion pipelineVersion =
                new PipelineVersion(1L);

        final ExecutionId executionId =
                new ExecutionId(
                        "recovery-runtime-execution");

        final ResourceId sourceResourceId =
                new ResourceId(
                        "recovery-runtime-source");

        final ResourceId destinationResourceId =
                new ResourceId(
                        "recovery-runtime-destination");

        final Flow flow =
                new Flow(
                        new FlowId(
                                "recovery-runtime-flow"),
                        "Recovery Runtime Flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                new Resource(
                        sourceResourceId,
                        new ResourceLocation(
                                "file:///recovery/input/source.txt"));

        final Resource destination =
                new Resource(
                        destinationResourceId,
                        new ResourceLocation(
                                "file:///recovery/output/destination.txt"));

        final PipelineConfiguration configuration =
                new PipelineConfiguration(
                        new PipelineDefinition(
                                pipelineId,
                                pipelineVersion,
                                "Recovery runtime pipeline"),
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

        executionRepository.save(
                Execution.rehydrate(
                        executionReference,
                        ExecutionStatus.RUNNING,
                        Instant.parse(
                                "2026-09-28T00:10:00Z"),
                        null));

        final DestinationExecutionReference
                destinationExecutionReference =
                new DestinationExecutionReference(
                        executionId,
                        destinationResourceId);

        destinationExecutionRepository.save(
                DestinationExecution.rehydrate(
                        destinationExecutionReference,
                        DestinationExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-28T00:10:00Z"),
                        Instant.parse(
                                "2026-09-28T00:10:10Z")));

        final DestinationCommitReference
                destinationCommitReference =
                new DestinationCommitReference(
                        destinationExecutionReference);

        destinationCommitRepository.save(
                DestinationCommit.notCommitted(
                        destinationCommitReference));

        destinationRecoveryExecutor.execute(
                destinationExecutionReference);

        final Path destinationPath =
                storageRoot
                        .resolve("recovery")
                        .resolve("output")
                        .resolve("destination.txt");

        assertTrue(
                Files.exists(destinationPath));

        assertEquals(
                "Ozhuku destination recovery integration test",
                Files.readString(destinationPath));

        final DestinationCommit recoveredCommit =
                destinationCommitRepository
                        .findById(
                                destinationCommitReference)
                        .orElseThrow();

        assertEquals(
                CommitStatus.COMMITTED,
                recoveredCommit.status());

        assertTrue(
                recoveredCommit.committedAt() != null);
    }
}

package io.github.avinashio.ozhuku.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.avinashio.ozhuku.application.pipeline.PipelineConfigurationResolutionService;
import io.github.avinashio.ozhuku.application.pipeline.PipelinePlanResolver;
import io.github.avinashio.ozhuku.application.recovery.DestinationExecutionRecoveryPolicy;
import io.github.avinashio.ozhuku.application.recovery.DestinationOutcomeInspector;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryDecider;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryExecutor;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
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
import io.github.avinashio.ozhuku.persistence.postgres.PostgresDestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresDestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresPipelineConfigurationRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DestinationRecoveryIT {

    private static final Instant STARTED_AT =
            Instant.parse("2026-09-28T00:00:00Z");

    private static final Instant FAILED_AT =
            Instant.parse("2026-09-28T00:00:10Z");

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-recovery-it");

    private static final PipelineId PIPELINE_ID =
            new PipelineId("pipeline-recovery-it");

    private static final PipelineVersion PIPELINE_VERSION =
            new PipelineVersion(1L);

    private static final ResourceId SOURCE_RESOURCE_ID =
            new ResourceId("source-recovery-it");

    private static final ResourceId DESTINATION_RESOURCE_ID =
            new ResourceId("destination-recovery-it");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static DataSource dataSource;

    private static PostgresExecutionRepository executionRepository;

    private static PostgresDestinationExecutionRepository
            destinationExecutionRepository;

    private static PostgresDestinationCommitRepository
            destinationCommitRepository;

    private static PostgresPipelineConfigurationRepository
            pipelineConfigurationRepository;

    @BeforeAll
    static void setUp() {
        dataSource = createDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        executionRepository =
                new PostgresExecutionRepository(dataSource);

        destinationExecutionRepository =
                new PostgresDestinationExecutionRepository(dataSource);

        destinationCommitRepository =
                new PostgresDestinationCommitRepository(dataSource);

        pipelineConfigurationRepository =
                new PostgresPipelineConfigurationRepository(dataSource);
    }

    @Test
    void shouldPreserveUnknownCommitAfterRecoveryExecutorReconstruction()
            throws IOException {

        final DestinationExecutionReference reference =
                destinationExecutionReference();

        final DestinationCommitReference commitReference =
                new DestinationCommitReference(reference);

        executionRepository.save(
                new Execution(executionReference()).start(STARTED_AT));

        destinationExecutionRepository.save(
                new DestinationExecution(reference)
                        .start(STARTED_AT)
                        .fail(FAILED_AT));

        pipelineConfigurationRepository.save(configuration());

        destinationCommitRepository.save(
                DestinationCommit.unknown(commitReference));

        assertEquals(
                CommitStatus.UNKNOWN,
                destinationCommitRepository.findById(commitReference)
                        .orElseThrow()
                        .status());

        final AtomicInteger transferCount = new AtomicInteger();

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        resource -> new ByteArrayInputStream(
                                "recovery-test".getBytes(
                                        java.nio.charset.StandardCharsets.UTF_8)),
                        (destination, content, conflictBehavior) -> {
                            transferCount.incrementAndGet();
                            content.transferTo(OutputStream.nullOutputStream());
                        });

        // Avinash: Reconstruct recovery services using only persisted state.
        final DestinationRecoveryExecutor restartedExecutor =
                createExecutor(transferService);

        restartedExecutor.execute(reference);

        assertEquals(0, transferCount.get());

        final DestinationCommit persistedCommit =
                destinationCommitRepository.findById(commitReference)
                        .orElseThrow();

        assertEquals(CommitStatus.UNKNOWN, persistedCommit.status());
    }

    private static DestinationRecoveryExecutor createExecutor(
            final ResourceTransferService transferService) {

        final DestinationOutcomeInspector inspector =
                ignoredReference -> CommitStatus.UNKNOWN;

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        final DestinationRecoveryService recoveryService =
                new DestinationRecoveryService(
                        destinationExecutionRepository,
                        destinationCommitRepository,
                        new DestinationExecutionRecoveryPolicy(),
                        decider);

        final PipelineConfigurationResolutionService
                configurationResolutionService =
                new PipelineConfigurationResolutionService(
                        pipelineConfigurationRepository,
                        new PipelinePlanResolver());

        return new DestinationRecoveryExecutor(
                recoveryService,
                executionRepository,
                configurationResolutionService,
                transferService,
                destinationCommitRepository,
                Clock.fixed(FAILED_AT, ZoneOffset.UTC));
    }

    private static DataSource createDataSource() {
        final PGSimpleDataSource source = new PGSimpleDataSource();
        source.setURL(POSTGRES.getJdbcUrl());
        source.setUser(POSTGRES.getUsername());
        source.setPassword(POSTGRES.getPassword());
        return source;
    }

    private static ExecutionReference executionReference() {
        return new ExecutionReference(
                EXECUTION_ID,
                PIPELINE_ID,
                PIPELINE_VERSION);
    }

    private static DestinationExecutionReference
    destinationExecutionReference() {

        return new DestinationExecutionReference(
                EXECUTION_ID,
                DESTINATION_RESOURCE_ID);
    }

    private static PipelineConfiguration configuration() {
        return new PipelineConfiguration(
                new PipelineDefinition(
                        PIPELINE_ID,
                        PIPELINE_VERSION,
                        "PostgreSQL recovery integration test"),
                new Flow(
                        new FlowId("flow-recovery-it"),
                        "Recovery integration flow",
                        FlowMode.RESOURCE_TRANSFER),
                new Resource(
                        SOURCE_RESOURCE_ID,
                        new ResourceLocation("file:///input/source.csv")),
                new Resource(
                        DESTINATION_RESOURCE_ID,
                        new ResourceLocation(
                                "file:///output/destination.csv")),
                new DeliveryPolicy(ConflictBehavior.REPLACE),
                DuplicatePolicy.SKIP_IF_PROCESSED);
    }
}

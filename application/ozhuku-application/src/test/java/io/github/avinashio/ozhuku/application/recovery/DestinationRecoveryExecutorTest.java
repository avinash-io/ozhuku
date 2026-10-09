package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.github.avinashio.ozhuku.application.pipeline.PipelineConfigurationResolutionService;
import io.github.avinashio.ozhuku.application.pipeline.PipelinePlanResolver;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;

class DestinationRecoveryExecutorTest {

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-recovery");

    private static final PipelineId PIPELINE_ID =
            new PipelineId("pipeline-1");

    private static final PipelineVersion PIPELINE_VERSION =
            new PipelineVersion(1L);

    private static final ResourceId SOURCE_RESOURCE_ID =
            new ResourceId("source");

    private static final ResourceId DESTINATION_RESOURCE_ID =
            new ResourceId("destination");

    private static final Instant COMMITTED_AT =
            Instant.parse("2026-09-28T00:00:00Z");

    @Test
    void shouldExecuteRetryAndPersistCommittedState()
            throws IOException {

        final DestinationExecutionReference reference =
                destinationExecutionReference();

        final InMemoryExecutionRepository executionRepository =
                new InMemoryExecutionRepository();

        final InMemoryDestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveExecution(executionRepository);

        saveFailedDestinationExecution(
                destinationExecutionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final InMemoryPipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();

        configurationRepository.save(configuration());

        final AtomicInteger transferCount =
                new AtomicInteger();
        final AtomicReference<CommitStatus> statusAtTransfer =
                new AtomicReference<>();

        final ResourceTransferService
                resourceTransferService =
                new ResourceTransferService(
                        resource ->
                                new java.io.ByteArrayInputStream(
                                        "test".getBytes()),
                        (destination, content, conflictBehavior) -> {
                            transferCount.incrementAndGet();
                            statusAtTransfer.set(
                                    commitRepository
                                            .findById(
                                                    new DestinationCommitReference(
                                                            reference))
                                            .orElseThrow()
                                            .status());
                            content.transferTo(
                                    java.io.OutputStream
                                            .nullOutputStream());
                        });

        final DestinationRecoveryExecutor executor =
                executor(
                        destinationExecutionRepository,
                        commitRepository,
                        executionRepository,
                        configurationRepository,
                        resourceTransferService);

        executor.execute(reference);

        assertEquals(
                1,
                transferCount.get());
        assertEquals(
                CommitStatus.UNKNOWN,
                statusAtTransfer.get());

        final DestinationCommit committed =
                commitRepository
                        .findById(
                                new DestinationCommitReference(
                                        reference))
                        .orElseThrow();

        assertEquals(
                CommitStatus.COMMITTED,
                committed.status());

        assertEquals(
                COMMITTED_AT,
                committed.committedAt());
    }

    @Test
    void shouldNotExecuteWhenRecoveryDecisionIsDoNotRetry()
            throws IOException {

        final DestinationExecutionReference reference =
                destinationExecutionReference();

        final InMemoryExecutionRepository executionRepository =
                new InMemoryExecutionRepository();

        final InMemoryDestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveExecution(executionRepository);

        saveFailedDestinationExecution(
                destinationExecutionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.committed(
                        new DestinationCommitReference(reference),
                        COMMITTED_AT));

        final InMemoryPipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();

        configurationRepository.save(configuration());

        final AtomicInteger transferCount =
                new AtomicInteger();

        final ResourceTransferService
                resourceTransferService =
                new ResourceTransferService(
                        resource ->
                                new java.io.ByteArrayInputStream(
                                        "test".getBytes()),
                        (destination, content, conflictBehavior) -> {
                            transferCount.incrementAndGet();
                            content.transferTo(
                                    java.io.OutputStream
                                            .nullOutputStream());
                        });

        final DestinationRecoveryExecutor executor =
                executor(
                        destinationExecutionRepository,
                        commitRepository,
                        executionRepository,
                        configurationRepository,
                        resourceTransferService);

        executor.execute(reference);

        assertEquals(
                0,
                transferCount.get());

        assertEquals(
                CommitStatus.COMMITTED,
                commitRepository
                        .findById(
                                new DestinationCommitReference(
                                        reference))
                        .orElseThrow()
                        .status());
    }

    @Test
    void shouldNotExecuteWhenRecoveryDecisionIsUnresolved()
            throws IOException {

        final DestinationExecutionReference reference =
                destinationExecutionReference();

        final InMemoryExecutionRepository executionRepository =
                new InMemoryExecutionRepository();

        final InMemoryDestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveExecution(executionRepository);

        saveFailedDestinationExecution(
                destinationExecutionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.unknown(
                        new DestinationCommitReference(reference)));

        final InMemoryPipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();

        configurationRepository.save(configuration());

        final AtomicInteger transferCount =
                new AtomicInteger();

        final ResourceTransferService
                resourceTransferService =
                new ResourceTransferService(
                        resource ->
                                new java.io.ByteArrayInputStream(
                                        "test".getBytes()),
                        (destination, content, conflictBehavior) -> {
                            transferCount.incrementAndGet();
                            content.transferTo(
                                    java.io.OutputStream
                                            .nullOutputStream());
                        });

        final DestinationRecoveryExecutor executor =
                executor(
                        destinationExecutionRepository,
                        commitRepository,
                        executionRepository,
                        configurationRepository,
                        resourceTransferService,
                        ignoredReference ->
                                CommitStatus.UNKNOWN);

        executor.execute(reference);

        assertEquals(
                0,
                transferCount.get());

        assertEquals(
                CommitStatus.UNKNOWN,
                commitRepository
                        .findById(
                                new DestinationCommitReference(
                                        reference))
                        .orElseThrow()
                        .status());
    }

    @Test
    void shouldNotPersistCommittedStateWhenTransferFails() {

        final DestinationExecutionReference reference =
                destinationExecutionReference();

        final InMemoryExecutionRepository executionRepository =
                new InMemoryExecutionRepository();

        final InMemoryDestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveExecution(executionRepository);

        saveFailedDestinationExecution(
                destinationExecutionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final InMemoryPipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();

        configurationRepository.save(configuration());

        final ResourceTransferService
                resourceTransferService =
                new ResourceTransferService(
                        resource ->
                                new java.io.ByteArrayInputStream(
                                        "test".getBytes()),
                        (destination, content, conflictBehavior) -> {
                            throw new IOException(
                                    "Simulated delivery failure");
                        });

        final DestinationRecoveryExecutor executor =
                executor(
                        destinationExecutionRepository,
                        commitRepository,
                        executionRepository,
                        configurationRepository,
                        resourceTransferService);

        assertThrows(
                IOException.class,
                () -> executor.execute(reference));

        assertEquals(
                CommitStatus.UNKNOWN,
                commitRepository
                        .findById(
                                new DestinationCommitReference(
                                        reference))
                        .orElseThrow()
                        .status());
    }

    @Test
    void shouldNotTransferWhenPersistingUnknownStateFails()
            throws IOException {

        final DestinationExecutionReference reference =
                destinationExecutionReference();

        final InMemoryExecutionRepository executionRepository =
                new InMemoryExecutionRepository();

        final InMemoryDestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository commitRepository =
                new InMemoryDestinationCommitRepository();
        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));
        commitRepository.failWhenSavingUnknown = true;

        saveExecution(executionRepository);
        saveFailedDestinationExecution(
                destinationExecutionRepository,
                reference);

        final InMemoryPipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();
        configurationRepository.save(configuration());

        final AtomicInteger transferCount = new AtomicInteger();

        final ResourceTransferService resourceTransferService =
                new ResourceTransferService(
                        resource ->
                                new java.io.ByteArrayInputStream(
                                        "test".getBytes()),
                        (destination, content, conflictBehavior) -> {
                            transferCount.incrementAndGet();
                            content.transferTo(
                                    java.io.OutputStream.nullOutputStream());
                        });

        final DestinationRecoveryExecutor executor =
                executor(
                        destinationExecutionRepository,
                        commitRepository,
                        executionRepository,
                        configurationRepository,
                        resourceTransferService);

        assertThrows(
                IllegalStateException.class,
                () -> executor.execute(reference));

        assertEquals(0, transferCount.get());
        assertEquals(
                CommitStatus.NOT_COMMITTED,
                commitRepository
                        .findById(new DestinationCommitReference(reference))
                        .orElseThrow()
                        .status());
    }

    @Test
    void shouldFailWhenExecutionDoesNotExist()
            throws IOException {

        final DestinationExecutionReference reference =
                destinationExecutionReference();

        final InMemoryDestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        final InMemoryExecutionRepository executionRepository =
                new InMemoryExecutionRepository();

        saveFailedDestinationExecution(
                destinationExecutionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final InMemoryPipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();

        configurationRepository.save(configuration());

        final DestinationRecoveryExecutor executor =
                executor(
                        destinationExecutionRepository,
                        commitRepository,
                        executionRepository,
                        configurationRepository,
                        new ResourceTransferService(
                                resource ->
                                        new java.io.ByteArrayInputStream(
                                                "test".getBytes()),
                                (destination, content, conflictBehavior) ->
                                        content.transferTo(
                                                java.io.OutputStream
                                                        .nullOutputStream())));

        assertThrows(
                IllegalStateException.class,
                () -> executor.execute(reference));
    }

    private DestinationRecoveryExecutor executor(
            final DestinationExecutionRepository
                    destinationExecutionRepository,
            final DestinationCommitRepository commitRepository,
            final ExecutionRepository executionRepository,
            final PipelineConfigurationRepository
                    configurationRepository,
            final ResourceTransferService
                    resourceTransferService) {

        return executor(
                destinationExecutionRepository,
                commitRepository,
                executionRepository,
                configurationRepository,
                resourceTransferService,
                ignoredReference -> CommitStatus.UNKNOWN);
    }

    private DestinationRecoveryExecutor executor(
            final DestinationExecutionRepository
                    destinationExecutionRepository,
            final DestinationCommitRepository commitRepository,
            final ExecutionRepository executionRepository,
            final PipelineConfigurationRepository
                    configurationRepository,
            final ResourceTransferService
                    resourceTransferService,
            final DestinationOutcomeInspector inspector) {

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        final DestinationRecoveryService recoveryService =
                new DestinationRecoveryService(
                        destinationExecutionRepository,
                        commitRepository,
                        new DestinationExecutionRecoveryPolicy(),
                        decider);

        final PipelineConfigurationResolutionService
                configurationResolutionService =
                new PipelineConfigurationResolutionService(
                        configurationRepository,
                        new PipelinePlanResolver());

        return new DestinationRecoveryExecutor(
                recoveryService,
                executionRepository,
                configurationResolutionService,
                resourceTransferService,
                commitRepository,
                Clock.fixed(
                        COMMITTED_AT,
                        ZoneOffset.UTC));
    }

    private static void saveExecution(
            final InMemoryExecutionRepository repository) {

        repository.save(
                Execution.rehydrate(
                        executionReference(),
                        ExecutionStatus.RUNNING,
                        Instant.parse(
                                "2026-09-28T00:00:00Z"),
                        null));
    }

    private static void saveFailedDestinationExecution(
            final InMemoryDestinationExecutionRepository repository,
            final DestinationExecutionReference reference) {

        repository.save(
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-28T00:00:00Z"),
                        Instant.parse(
                                "2026-09-28T00:00:10Z")));
    }

    private static PipelineConfiguration configuration() {

        return new PipelineConfiguration(
                new PipelineDefinition(
                        PIPELINE_ID,
                        PIPELINE_VERSION,
                        "Test pipeline"),
                new Flow(
                        new FlowId("flow-1"),
                        "Test flow",
                        FlowMode.RESOURCE_TRANSFER),
                resource("source"),
                resource("destination"),
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE),
                DuplicatePolicy.SKIP_IF_PROCESSED);
    }

    private static Resource resource(
            final String name) {

        return new Resource(
                new ResourceId(name),
                new ResourceLocation(
                        "file:///" + name));
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

    private static final class
    InMemoryExecutionRepository
            implements ExecutionRepository {

        private final Map<ExecutionId, Execution>
                executions =
                new HashMap<>();

        @Override
        public Optional<Execution> findById(
                final ExecutionId executionId) {

            return Optional.ofNullable(
                    executions.get(executionId));
        }

        @Override
        public void save(
                final Execution execution) {

            executions.put(
                    execution.reference().executionId(),
                    execution);
        }
    }

    private static final class
    InMemoryDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private final Map<
                DestinationExecutionReference,
                DestinationExecution>
                executions =
                new HashMap<>();

        @Override
        public Optional<DestinationExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(
                            new DestinationExecutionReference(
                                    executionId,
                                    resourceId)));
        }

        @Override
        public void save(
                final DestinationExecution destinationExecution) {

            executions.put(
                    destinationExecution.reference(),
                    destinationExecution);
        }
    }

    private static final class
    InMemoryDestinationCommitRepository
            implements DestinationCommitRepository {

        private final Map<
                DestinationCommitReference,
                DestinationCommit>
                commits =
                new HashMap<>();

        private boolean failWhenSavingUnknown;

        @Override
        public Optional<DestinationCommit> findById(
                final DestinationCommitReference reference) {

            return Optional.ofNullable(
                    commits.get(reference));
        }

        @Override
        public void save(
                final DestinationCommit destinationCommit) {

            if (failWhenSavingUnknown
                    && destinationCommit.status() == CommitStatus.UNKNOWN) {
                throw new IllegalStateException(
                        "Simulated failure persisting unknown state");
            }

            commits.put(
                    destinationCommit.reference(),
                    destinationCommit);
        }
    }

    private static final class
    InMemoryPipelineConfigurationRepository
            implements PipelineConfigurationRepository {

        private final Map<String, PipelineConfiguration>
                configurations =
                new HashMap<>();

        @Override
        public Optional<PipelineConfiguration> findByVersion(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {

            return Optional.ofNullable(
                    configurations.get(
                            key(
                                    pipelineId,
                                    pipelineVersion)));
        }

        @Override
        public void save(
                final PipelineConfiguration configuration) {

            configurations.put(
                    key(
                            configuration.pipelineDefinition()
                                    .pipelineId(),
                            configuration.pipelineDefinition()
                                    .version()),
                    configuration);
        }

        private static String key(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {

            return pipelineId.value()
                    + ":"
                    + pipelineVersion.value();
        }
    }
}
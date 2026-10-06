package io.github.avinashio.ozhuku.server.config;

import io.github.avinashio.ozhuku.application.deduplication.DeduplicationService;
import io.github.avinashio.ozhuku.application.deduplication.ExecutionDeduplicationService;
import io.github.avinashio.ozhuku.application.execution.DestinationExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.ExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.ExecutionProcessingCoordinator;
import io.github.avinashio.ozhuku.application.execution.ExecutionResourceValidator;
import io.github.avinashio.ozhuku.application.execution.ExecutionRunService;
import io.github.avinashio.ozhuku.application.execution.FlowExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.PipelineExecutionService;
import io.github.avinashio.ozhuku.application.execution.SourceExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.initialization.ExecutionInitializationService;
import io.github.avinashio.ozhuku.application.orchestration.ExecutionOrchestrationService;
import io.github.avinashio.ozhuku.application.pipeline.ConfiguredPipelineExecutionService;
import io.github.avinashio.ozhuku.application.pipeline.PipelineConfigurationResolutionService;
import io.github.avinashio.ozhuku.application.pipeline.PipelinePlanResolver;
import io.github.avinashio.ozhuku.application.processing.ExecutionProcessingService;
import io.github.avinashio.ozhuku.application.record.RecordProcessingService;
import io.github.avinashio.ozhuku.application.recovery.DestinationExecutionRecoveryPolicy;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryDecider;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryExecutor;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryService;
import io.github.avinashio.ozhuku.application.recovery.DestinationRecoveryUseCase;
import io.github.avinashio.ozhuku.application.recovery.PersistenceDestinationOutcomeInspector;
import io.github.avinashio.ozhuku.application.source.SourceFingerprintService;
import io.github.avinashio.ozhuku.application.source.SourceIdentityService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationEvaluator;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import io.github.avinashio.ozhuku.format.csv.CsvFormatReader;
import io.github.avinashio.ozhuku.format.csv.CsvFormatWriter;
import io.github.avinashio.ozhuku.format.csv.CsvReaderConfiguration;
import io.github.avinashio.ozhuku.format.csv.CsvWriterConfiguration;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.PipelineConfigurationRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresDestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresDestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresFlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresPipelineConfigurationRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresProcessingRecordRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresSourceExecutionRepository;
import io.github.avinashio.ozhuku.storage.StorageOutputProvider;
import io.github.avinashio.ozhuku.storage.StorageReader;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import io.github.avinashio.ozhuku.storage.file.FileSourceFingerprintProvider;
import io.github.avinashio.ozhuku.storage.file.FileSourceIdentityProvider;
import io.github.avinashio.ozhuku.storage.file.FileStorageOutputProvider;
import io.github.avinashio.ozhuku.storage.file.FileStoragePathResolver;
import io.github.avinashio.ozhuku.storage.file.FileStorageReader;
import io.github.avinashio.ozhuku.storage.file.FileStorageWriter;
import java.nio.file.Path;
import java.time.Clock;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public FileStoragePathResolver fileStoragePathResolver(
            @Value("${ozhuku.storage.root}") final Path storageRoot) {
        return new FileStoragePathResolver(storageRoot);
    }

    @Bean
    public StorageReader storageReader(
            final FileStoragePathResolver pathResolver) {
        return new FileStorageReader(pathResolver);
    }

    @Bean
    public StorageWriter storageWriter(
            final FileStoragePathResolver pathResolver) {
        return new FileStorageWriter(pathResolver);
    }

    @Bean
    public StorageOutputProvider storageOutputProvider(
            final FileStoragePathResolver pathResolver) {
        return new FileStorageOutputProvider(pathResolver);
    }

    @Bean
    public CsvReaderConfiguration csvReaderConfiguration() {
        return CsvReaderConfiguration.defaults();
    }

    @Bean
    public CsvWriterConfiguration csvWriterConfiguration() {
        return CsvWriterConfiguration.defaults();
    }

    @Bean
    public FormatReader formatReader(
            final CsvReaderConfiguration configuration) {
        return new CsvFormatReader(configuration);
    }

    @Bean
    public FormatWriter formatWriter(
            final CsvWriterConfiguration configuration) {
        return new CsvFormatWriter(configuration);
    }

    @Bean
    public ExecutionRepository executionRepository(
            final DataSource dataSource) {
        return new PostgresExecutionRepository(dataSource);
    }

    @Bean
    public FlowExecutionRepository flowExecutionRepository(
            final DataSource dataSource) {
        return new PostgresFlowExecutionRepository(dataSource);
    }

    @Bean
    public SourceExecutionRepository sourceExecutionRepository(
            final DataSource dataSource) {
        return new PostgresSourceExecutionRepository(dataSource);
    }

    @Bean
    public DestinationExecutionRepository destinationExecutionRepository(
            final DataSource dataSource) {
        return new PostgresDestinationExecutionRepository(dataSource);
    }

    @Bean
    public DestinationCommitRepository destinationCommitRepository(
            final DataSource dataSource) {
        return new PostgresDestinationCommitRepository(dataSource);
    }

    @Bean
    public ProcessingRecordRepository processingRecordRepository(
            final DataSource dataSource) {
        return new PostgresProcessingRecordRepository(dataSource);
    }

    @Bean
    public ExecutionLifecycleService executionLifecycleService(
            final ExecutionRepository executionRepository,
            final Clock clock) {
        return new ExecutionLifecycleService(
                executionRepository,
                clock);
    }

    @Bean
    public FlowExecutionLifecycleService flowExecutionLifecycleService(
            final FlowExecutionRepository flowExecutionRepository,
            final Clock clock) {
        return new FlowExecutionLifecycleService(
                flowExecutionRepository,
                clock);
    }

    @Bean
    public SourceExecutionLifecycleService sourceExecutionLifecycleService(
            final SourceExecutionRepository sourceExecutionRepository,
            final Clock clock) {
        return new SourceExecutionLifecycleService(
                sourceExecutionRepository,
                clock);
    }

    @Bean
    public DestinationExecutionLifecycleService
    destinationExecutionLifecycleService(
            final DestinationExecutionRepository
                    destinationExecutionRepository,
            final Clock clock) {
        return new DestinationExecutionLifecycleService(
                destinationExecutionRepository,
                clock);
    }

    @Bean
    public ExecutionOrchestrationService executionOrchestrationService(
            final ExecutionLifecycleService executionLifecycleService,
            final FlowExecutionLifecycleService flowExecutionLifecycleService,
            final SourceExecutionLifecycleService
                    sourceExecutionLifecycleService,
            final DestinationExecutionLifecycleService
                    destinationExecutionLifecycleService) {
        return new ExecutionOrchestrationService(
                executionLifecycleService,
                flowExecutionLifecycleService,
                sourceExecutionLifecycleService,
                destinationExecutionLifecycleService);
    }

    @Bean
    public ExecutionInitializationService executionInitializationService(
            final ExecutionRepository executionRepository,
            final FlowExecutionRepository flowExecutionRepository,
            final SourceExecutionRepository sourceExecutionRepository,
            final DestinationExecutionRepository
                    destinationExecutionRepository,
            final DestinationCommitRepository
                    destinationCommitRepository) {
        return new ExecutionInitializationService(
                executionRepository,
                flowExecutionRepository,
                sourceExecutionRepository,
                destinationExecutionRepository,
                destinationCommitRepository);
    }

    @Bean
    public ExecutionResourceValidator executionResourceValidator(
            final SourceExecutionRepository sourceExecutionRepository,
            final DestinationExecutionRepository
                    destinationExecutionRepository) {
        return new ExecutionResourceValidator(
                sourceExecutionRepository,
                destinationExecutionRepository);
    }

    @Bean
    public ResourceTransferService resourceTransferService(
            final StorageReader storageReader,
            final StorageWriter storageWriter) {
        return new ResourceTransferService(
                storageReader,
                storageWriter);
    }

    @Bean
    public RecordProcessingService recordProcessingService(
            final StorageReader storageReader,
            final StorageOutputProvider storageOutputProvider) {
        return new RecordProcessingService(
                storageReader,
                storageOutputProvider);
    }

    @Bean
    public ExecutionProcessingService executionProcessingService(
            final ResourceTransferService resourceTransferService,
            final RecordProcessingService recordProcessingService) {
        return new ExecutionProcessingService(
                resourceTransferService,
                recordProcessingService);
    }

    @Bean
    public SourceIdentityService sourceIdentityService(
            final FileStoragePathResolver pathResolver) {
        return new SourceIdentityService(
                new FileSourceIdentityProvider(pathResolver));
    }

    @Bean
    public SourceFingerprintService sourceFingerprintService(
            final FileStoragePathResolver pathResolver) {
        return new SourceFingerprintService(
                new FileSourceFingerprintProvider(pathResolver));
    }

    @Bean
    public DeduplicationService deduplicationService(
            final ProcessingRecordRepository processingRecordRepository) {
        return new DeduplicationService(
                processingRecordRepository,
                new DeduplicationEvaluator());
    }

    @Bean
    public ExecutionDeduplicationService executionDeduplicationService(
            final SourceIdentityService sourceIdentityService,
            final SourceFingerprintService sourceFingerprintService,
            final DeduplicationService deduplicationService) {
        return new ExecutionDeduplicationService(
                sourceIdentityService,
                sourceFingerprintService,
                deduplicationService);
    }

    @Bean
    public ExecutionProcessingCoordinator executionProcessingCoordinator(
            final ExecutionOrchestrationService
                    executionOrchestrationService,
            final ExecutionProcessingService
                    executionProcessingService,
            final ExecutionResourceValidator
                    executionResourceValidator,
            final ExecutionDeduplicationService
                    executionDeduplicationService,
            final ProcessingRecordRepository
                    processingRecordRepository,
            final DestinationCommitRepository
                    destinationCommitRepository,
            final Clock clock) {
        return new ExecutionProcessingCoordinator(
                executionOrchestrationService,
                executionProcessingService,
                executionResourceValidator,
                executionDeduplicationService,
                processingRecordRepository,
                destinationCommitRepository,
                clock);
    }

    @Bean
    public ExecutionRunService executionRunService(
            final ExecutionInitializationService
                    executionInitializationService,
            final ExecutionProcessingCoordinator
                    executionProcessingCoordinator) {
        return new ExecutionRunService(
                executionInitializationService,
                executionProcessingCoordinator);
    }

    @Bean
    public PipelineExecutionService pipelineExecutionService(
            final ExecutionRunService executionRunService) {
        return new PipelineExecutionService(
                executionRunService);
    }

    @Bean
    public PipelineConfigurationRepository
    pipelineConfigurationRepository(
            final DataSource dataSource) {
        return new PostgresPipelineConfigurationRepository(dataSource);
    }

    @Bean
    public PipelinePlanResolver pipelinePlanResolver() {
        return new PipelinePlanResolver();
    }

    @Bean
    public PipelineConfigurationResolutionService
    pipelineConfigurationResolutionService(
            final PipelineConfigurationRepository
                    pipelineConfigurationRepository,
            final PipelinePlanResolver pipelinePlanResolver) {
        return new PipelineConfigurationResolutionService(
                pipelineConfigurationRepository,
                pipelinePlanResolver);
    }

    @Bean
    public ConfiguredPipelineExecutionService
    configuredPipelineExecutionService(
            final PipelineConfigurationResolutionService
                    configurationResolutionService,
            final PipelineExecutionService
                    pipelineExecutionService) {
        return new ConfiguredPipelineExecutionService(
                configurationResolutionService,
                pipelineExecutionService);
    }

    @Bean
    public DestinationExecutionRecoveryPolicy
    destinationExecutionRecoveryPolicy() {
        return new DestinationExecutionRecoveryPolicy();
    }

    @Bean
    public PersistenceDestinationOutcomeInspector
    destinationOutcomeInspector(
            final DestinationCommitRepository
                    destinationCommitRepository) {
        return new PersistenceDestinationOutcomeInspector(
                destinationCommitRepository);
    }

    @Bean
    public DestinationRecoveryDecider destinationRecoveryDecider(
            final PersistenceDestinationOutcomeInspector
                    outcomeInspector) {
        return new DestinationRecoveryDecider(
                outcomeInspector);
    }

    @Bean
    public DestinationRecoveryService destinationRecoveryService(
            final DestinationExecutionRepository
                    destinationExecutionRepository,
            final DestinationCommitRepository
                    destinationCommitRepository,
            final DestinationExecutionRecoveryPolicy
                    executionRecoveryPolicy,
            final DestinationRecoveryDecider recoveryDecider) {
        return new DestinationRecoveryService(
                destinationExecutionRepository,
                destinationCommitRepository,
                executionRecoveryPolicy,
                recoveryDecider);
    }

    @Bean
    public DestinationRecoveryUseCase destinationRecoveryUseCase(
            final DestinationRecoveryService
                    recoveryDecisionProvider) {
        return new DestinationRecoveryUseCase(
                recoveryDecisionProvider);
    }

    @Bean
    public DestinationRecoveryExecutor destinationRecoveryExecutor(
            final DestinationRecoveryService
                    destinationRecoveryService,
            final ExecutionRepository executionRepository,
            final PipelineConfigurationResolutionService
                    pipelineConfigurationResolutionService,
            final ResourceTransferService resourceTransferService,
            final DestinationCommitRepository
                    destinationCommitRepository,
            final Clock clock) {
        return new DestinationRecoveryExecutor(
                destinationRecoveryService,
                executionRepository,
                pipelineConfigurationResolutionService,
                resourceTransferService,
                destinationCommitRepository,
                clock);
    }
}
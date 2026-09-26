package io.github.avinashio.ozhuku.server.config;

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
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresDestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresDestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresFlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresProcessingRecordRepository;
import io.github.avinashio.ozhuku.persistence.postgres.PostgresSourceExecutionRepository;
import io.github.avinashio.ozhuku.storage.StorageOutputProvider;
import io.github.avinashio.ozhuku.storage.StorageReader;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import io.github.avinashio.ozhuku.storage.file.FileStorageOutputProvider;
import io.github.avinashio.ozhuku.storage.file.FileStoragePathResolver;
import io.github.avinashio.ozhuku.storage.file.FileStorageReader;
import io.github.avinashio.ozhuku.storage.file.FileStorageWriter;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfiguration {

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
}
package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionResourceValidatorTest {

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final ResourceId SOURCE_RESOURCE_ID =
            new ResourceId("source-1");

    private static final ResourceId DESTINATION_RESOURCE_ID =
            new ResourceId("destination-1");

    private FakeSourceExecutionRepository sourceRepository;
    private FakeDestinationExecutionRepository destinationRepository;

    private ExecutionResourceValidator validator;

    @BeforeEach
    void setUp() {
        sourceRepository =
                new FakeSourceExecutionRepository();

        destinationRepository =
                new FakeDestinationExecutionRepository();

        validator = new ExecutionResourceValidator(
                sourceRepository,
                destinationRepository);
    }

    @Test
    void validateShouldSucceedWhenResourcesMatchExecution() {
        saveSourceExecution(SOURCE_RESOURCE_ID);
        saveDestinationExecution(DESTINATION_RESOURCE_ID);

        assertDoesNotThrow(
                () -> validator.validate(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));
    }

    @Test
    void validateShouldFailWhenSourceResourceDoesNotMatch() {
        saveSourceExecution(
                new ResourceId("different-source"));
        saveDestinationExecution(DESTINATION_RESOURCE_ID);

        assertThrows(
                IllegalStateException.class,
                () -> validator.validate(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));
    }

    @Test
    void validateShouldFailWhenDestinationResourceDoesNotMatch() {
        saveSourceExecution(SOURCE_RESOURCE_ID);
        saveDestinationExecution(
                new ResourceId("different-destination"));

        assertThrows(
                IllegalStateException.class,
                () -> validator.validate(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));
    }

    @Test
    void validateShouldFailWhenSourceExecutionDoesNotExist() {
        saveDestinationExecution(DESTINATION_RESOURCE_ID);

        assertThrows(
                IllegalStateException.class,
                () -> validator.validate(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));
    }

    @Test
    void validateShouldFailWhenDestinationExecutionDoesNotExist() {
        saveSourceExecution(SOURCE_RESOURCE_ID);

        assertThrows(
                IllegalStateException.class,
                () -> validator.validate(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));
    }

    @Test
    void validateShouldRejectNullExecutionId() {
        assertThrows(
                NullPointerException.class,
                () -> validator.validate(
                        null,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));
    }

    @Test
    void validateShouldRejectNullSourceResourceId() {
        assertThrows(
                NullPointerException.class,
                () -> validator.validate(
                        EXECUTION_ID,
                        null,
                        DESTINATION_RESOURCE_ID));
    }

    @Test
    void validateShouldRejectNullDestinationResourceId() {
        assertThrows(
                NullPointerException.class,
                () -> validator.validate(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID,
                        null));
    }

    private void saveSourceExecution(
            final ResourceId resourceId) {

        sourceRepository.save(
                new SourceExecution(
                        new SourceExecutionReference(
                                EXECUTION_ID,
                                resourceId)));
    }

    private void saveDestinationExecution(
            final ResourceId resourceId) {

        destinationRepository.save(
                new DestinationExecution(
                        new DestinationExecutionReference(
                                EXECUTION_ID,
                                resourceId)));
    }

    private static final class FakeSourceExecutionRepository
            implements SourceExecutionRepository {

        private final Map<String, SourceExecution> executions =
                new HashMap<>();

        @Override
        public Optional<SourceExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(
                            key(
                                    executionId,
                                    resourceId)));
        }

        @Override
        public void save(
                final SourceExecution sourceExecution) {

            executions.put(
                    key(
                            sourceExecution.reference().executionId(),
                            sourceExecution.reference().resourceId()),
                    sourceExecution);
        }

        private static String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return executionId + ":" + resourceId;
        }
    }

    private static final class FakeDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private final Map<String, DestinationExecution> executions =
                new HashMap<>();

        @Override
        public Optional<DestinationExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(
                            key(
                                    executionId,
                                    resourceId)));
        }

        @Override
        public void save(
                final DestinationExecution destinationExecution) {

            executions.put(
                    key(
                            destinationExecution.reference().executionId(),
                            destinationExecution.reference().resourceId()),
                    destinationExecution);
        }

        private static String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return executionId + ":" + resourceId;
        }
    }
}
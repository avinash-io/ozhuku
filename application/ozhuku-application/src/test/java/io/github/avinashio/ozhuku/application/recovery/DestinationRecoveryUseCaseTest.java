package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import org.junit.jupiter.api.Test;

class DestinationRecoveryUseCaseTest {

    @Test
    void shouldDelegateRecoveryDecisionToProvider() {
        final RecoveryResult expected =
                new RecoveryResult(
                        RecoveryDecision.RETRY,
                        RecoveryReason.COMMIT_NOT_CONFIRMED);

        final DestinationExecutionReference reference =
                new DestinationExecutionReference(
                        new ExecutionId("execution-1"),
                        new ResourceId("resource-1"));

        final StubRecoveryDecisionProvider provider =
                new StubRecoveryDecisionProvider(
                        reference,
                        expected);

        final DestinationRecoveryUseCase useCase =
                new DestinationRecoveryUseCase(provider);

        final RecoveryResult actual =
                useCase.execute(reference);

        assertEquals(expected, actual);
    }

    @Test
    void shouldRejectNullProvider() {
        assertThrows(
                NullPointerException.class,
                () -> new DestinationRecoveryUseCase(null));
    }

    @Test
    void shouldRejectNullReference() {
        final DestinationRecoveryUseCase useCase =
                new DestinationRecoveryUseCase(
                        new StubRecoveryDecisionProvider(
                                null,
                                null));

        assertThrows(
                NullPointerException.class,
                () -> useCase.execute(null));
    }

    private static final class StubRecoveryDecisionProvider
            implements RecoveryDecisionProvider {

        private final DestinationExecutionReference reference;
        private final RecoveryResult result;

        private StubRecoveryDecisionProvider(
                final DestinationExecutionReference reference,
                final RecoveryResult result) {

            this.reference = reference;
            this.result = result;
        }

        @Override
        public RecoveryResult decide(
                final DestinationExecutionReference requestedReference) {

            if (reference != null
                    && reference.equals(requestedReference)) {
                return result;
            }

            return result;
        }
    }
}

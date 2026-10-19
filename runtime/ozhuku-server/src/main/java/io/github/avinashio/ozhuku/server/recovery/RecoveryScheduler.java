package io.github.avinashio.ozhuku.server.recovery;

import io.github.avinashio.ozhuku.application.recovery.RecoveryOrchestrationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for durable execution‑state recovery and failure handling.
 *
 * Implements the pending task:
 *   "Complete durable execution‑state recovery and failure handling".
 * Calls RecoveryOrchestrationService#processRecoverableCandidates() periodically.
 */
@Component
public class RecoveryScheduler {

    private final RecoveryOrchestrationService recoveryOrchestrationService;

    public RecoveryScheduler(final RecoveryOrchestrationService recoveryOrchestrationService) {
        this.recoveryOrchestrationService = recoveryOrchestrationService;
    }

    @jakarta.annotation.PostConstruct
    public void initialRecovery() {
        recoveryOrchestrationService.processRecoverableCandidates();
    }

    @Scheduled(fixedDelayString = "${ozhuku.recovery.fixedDelay:300000}")
    public void scheduledRecovery() {
        recoveryOrchestrationService.processRecoverableCandidates();
    }
}

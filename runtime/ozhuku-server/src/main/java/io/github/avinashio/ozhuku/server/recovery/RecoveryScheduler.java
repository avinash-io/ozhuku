package io.github.avinashio.ozhuku.server.recovery;

import io.github.avinashio.ozhuku.application.recovery.RecoveryOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(RecoveryScheduler.class);

    private final RecoveryOrchestrationService recoveryOrchestrationService;

    public RecoveryScheduler(final RecoveryOrchestrationService recoveryOrchestrationService) {
        this.recoveryOrchestrationService = recoveryOrchestrationService;
    }

    @jakarta.annotation.PostConstruct
    public void initialRecovery() {
        try {
            recoveryOrchestrationService.processRecoverableCandidates();
        } catch (Exception e) {
            // During tests or early startup, database tables may not exist yet.
            // Log and continue to avoid breaking application startup.
            log.warn("Initial recovery skipped: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${ozhuku.recovery.fixedDelay:300000}")
    public void scheduledRecovery() {
        try {
            recoveryOrchestrationService.processRecoverableCandidates();
        } catch (Exception e) {
            log.warn("Scheduled recovery skipped: {}", e.getMessage());
        }
    }
}

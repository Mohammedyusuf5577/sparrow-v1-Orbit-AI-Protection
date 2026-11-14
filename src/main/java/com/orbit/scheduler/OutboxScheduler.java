package com.orbit.scheduler;

import com.orbit.service.OutboxWorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxWorkerService outboxWorkerService;

    @Scheduled(fixedDelayString = "${app.outbox.scheduler.rate:30000}")
    public void runOutboxWorker() {
        try {
            outboxWorkerService.processOutboxBatch();
        } catch (Exception e) {
            log.error("OutboxScheduler encountered an unhandled exception", e);
        }
    }
}

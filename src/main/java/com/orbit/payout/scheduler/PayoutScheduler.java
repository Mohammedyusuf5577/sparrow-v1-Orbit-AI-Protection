package com.orbit.payout.scheduler;

import com.orbit.payout.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PayoutScheduler {

    private final PayoutService payoutService;

    // Runs every day at 9:00 AM (configured via application.properties)
    @Scheduled(cron = "${app.payroll.scheduler.cron}")
    public void processDailySalaries() {
        log.info("=== Starting Daily Salary Payout Processing ===");
        try {
            payoutService.processAllPendingPayoutsForToday();
            log.info("=== Daily Salary Payout Processing Completed ===");
        } catch (Exception e) {
            log.error("=== Error occurred during daily salary payout processing ===", e);
        }
    }
}

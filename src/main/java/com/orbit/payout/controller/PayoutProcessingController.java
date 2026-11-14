package com.orbit.payout.controller;

import com.orbit.dto.response.ApiResponse;
import com.orbit.payout.dto.response.PayoutTransactionResponse;
import com.orbit.payout.service.PayoutService;
import com.orbit.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
@Tag(name = "Payout Processing", description = "Trigger Payout APIs")
public class PayoutProcessingController {

    private final PayoutService payoutService;

    @PostMapping("/process/{employeeId}")
    @Operation(summary = "Manually trigger salary payout for an employee")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutTransactionResponse>> processPayout(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYOUT_INITIATED.getMessage(),
                payoutService.processPayout(employeeId)));
    }

    @PostMapping("/process-all")
    @Operation(summary = "Trigger payouts for all pending records up to today")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> processAllPayouts() {
        payoutService.processAllPendingPayoutsForToday();
        return ResponseEntity.ok(ApiResponse.success(AppUtils.Status.PAYOUT_ALL_TRIGGERED.getMessage()));
    }

    @GetMapping("/history/{employeeId}")
    @Operation(summary = "View payout history of an employee")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayoutTransactionResponse>>> getPayoutHistory(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYOUT_TRANSACTIONS_FETCHED.getMessage(),
                payoutService.getPayoutHistoryByEmployee(employeeId)));
    }
}

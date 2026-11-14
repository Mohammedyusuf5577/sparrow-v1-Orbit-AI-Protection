package com.orbit.payout.controller;

import com.orbit.dto.response.ApiResponse;
import com.orbit.payout.dto.request.CreatePayoutRecordRequest;
import com.orbit.payout.dto.request.RegisterBankDetailsRequest;
import com.orbit.payout.dto.response.BankRegistrationResponse;
import com.orbit.payout.dto.response.PayoutRecordResponse;
import com.orbit.payout.dto.response.PayoutTransactionResponse;
import com.orbit.payout.service.PayoutService;
import com.orbit.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payouts")
@RequiredArgsConstructor
@Tag(name = "Admin Payouts", description = "Admin Payout APIs for Salary processing")
public class AdminPayoutController {

    private final PayoutService payoutService;

    @PostMapping("/bank-details")
    @Operation(summary = "Register bank details with Razorpay")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<BankRegistrationResponse>> registerBankDetails(
            @Valid @RequestBody RegisterBankDetailsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.BANK_DETAILS_REGISTERED.getMessage(),
                payoutService.registerBankDetails(request)));
    }

    @PostMapping("/records")
    @Operation(summary = "Create a new salary payout record")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutRecordResponse>> createPayoutRecord(
            @Valid @RequestBody CreatePayoutRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYOUT_RECORD_CREATED.getMessage(),
                payoutService.createPayoutRecord(request)));
    }

    @GetMapping("/records")
    @Operation(summary = "Get all scheduled payout records")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayoutRecordResponse>>> getAllRecords() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYOUT_RECORDS_FETCHED.getMessage(),
                payoutService.getAllPayoutRecords()));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all payout transactions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayoutTransactionResponse>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYOUT_TRANSACTIONS_FETCHED.getMessage(),
                payoutService.getAllPayoutTransactions()));
    }
}

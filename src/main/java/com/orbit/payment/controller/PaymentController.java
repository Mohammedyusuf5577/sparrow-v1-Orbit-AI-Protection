package com.orbit.payment.controller;

import com.orbit.dto.response.ApiResponse;
import com.orbit.payment.dto.request.CreateOrderRequest;
import com.orbit.payment.dto.request.PaymentVerifyRequest;
import com.orbit.payment.dto.response.PaymentOrderResponse;
import com.orbit.payment.dto.response.PaymentResponse;
import com.orbit.payment.service.PaymentService;
import com.orbit.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Razorpay Payment Integration APIs")
public class PaymentController {

    private final PaymentService paymentService;

    // ─────────── CREATE ORDER ───────────
    @PostMapping("/create-order")
    @Operation(summary = "Create a Razorpay payment order for salary disbursement")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        PaymentOrderResponse response = paymentService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppUtils.Status.PAYMENT_ORDER_CREATED.getMessage(), response));
    }

    // ─────────── VERIFY PAYMENT ───────────
    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature and confirm transaction")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {
        PaymentResponse response = paymentService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.success(AppUtils.Status.PAYMENT_VERIFIED.getMessage(), response));
    }

    // ─────────── WEBHOOK ───────────
    /**
     * Razorpay sends a POST with JSON body and "X-Razorpay-Signature" header.
     * This endpoint must be excluded from CSRF / JWT filtering (see SecurityConfig).
     */
    @PostMapping("/webhook")
    @Operation(summary = "Razorpay webhook endpoint – do NOT call manually")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.success(AppUtils.Status.WEBHOOK_PROCESSED.getMessage()));
    }

    // ─────────── HISTORY ───────────
    @GetMapping("/payroll/{payrollId}")
    @Operation(summary = "Get all payments for a specific payroll record")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByPayroll(
            @PathVariable UUID payrollId) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYMENTS_FETCHED.getMessage(),
                paymentService.getPaymentsByPayroll(payrollId)));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get all payments for a specific employee")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYMENTS_FETCHED.getMessage(),
                paymentService.getPaymentsByEmployee(employeeId)));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details by payment ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.PAYMENTS_FETCHED.getMessage(),
                paymentService.getPaymentById(paymentId)));
    }

    // ─────────── REFUND ───────────
    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Initiate a full refund for a successful payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiateRefund(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(ApiResponse.success(
                AppUtils.Status.REFUND_INITIATED.getMessage(),
                paymentService.initiateRefund(paymentId)));
    }
}

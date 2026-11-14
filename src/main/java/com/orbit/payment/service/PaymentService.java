package com.orbit.payment.service;

import com.orbit.payment.dto.request.CreateOrderRequest;
import com.orbit.payment.dto.request.PaymentVerifyRequest;
import com.orbit.payment.dto.response.PaymentOrderResponse;
import com.orbit.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    /**
     * Creates a Razorpay order and persists a CREATED payment record in DB.
     */
    PaymentOrderResponse createOrder(CreateOrderRequest request);

    /**
     * Verifies Razorpay HMAC signature and marks payment SUCCESS or FAILED.
     */
    PaymentResponse verifyPayment(PaymentVerifyRequest request);

    /**
     * Idempotent webhook handler (payment.captured / payment.failed events).
     */
    void handleWebhook(String payload, String signature);

    /**
     * Fetch all payments for a given payroll record.
     */
    List<PaymentResponse> getPaymentsByPayroll(UUID payrollId);

    /**
     * Fetch all payments for an employee (by user ID).
     */
    List<PaymentResponse> getPaymentsByEmployee(UUID employeeId);

    /**
     * Get a single payment by its internal UUID.
     */
    PaymentResponse getPaymentById(UUID paymentId);

    /**
     * Initiate a Razorpay refund for a successful payment.
     */
    PaymentResponse initiateRefund(UUID paymentId);
}

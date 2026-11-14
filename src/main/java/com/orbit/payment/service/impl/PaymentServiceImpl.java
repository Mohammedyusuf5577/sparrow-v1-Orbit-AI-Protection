package com.orbit.payment.service.impl;

import com.orbit.exception.AppException;
import com.orbit.payment.dto.request.CreateOrderRequest;
import com.orbit.payment.dto.request.PaymentVerifyRequest;
import com.orbit.payment.dto.response.PaymentOrderResponse;
import com.orbit.payment.dto.response.PaymentResponse;
import com.orbit.payment.entity.PaymentEntity;
import com.orbit.payment.repository.PaymentRepository;
import com.orbit.payment.service.PaymentService;
import com.orbit.payroll.entity.EmployeePayrollEntity;
import com.orbit.payroll.repository.PayrollSalaryStructureRepository;
import com.orbit.util.AppUtils;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PayrollSalaryStructureRepository salaryStructureRepository;
    private final RazorpayClient razorpayClient;
    private final EntityManager entityManager;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE ORDER
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentOrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating Razorpay order for payrollId={}", request.getPayrollId());

        EmployeePayrollEntity payroll = entityManager.find(EmployeePayrollEntity.class, request.getPayrollId());
        if (payroll == null) {
            throw new AppException(AppUtils.Status.PAYROLL_NOT_FOUND);
        }

        // Resolve amount: use provided amount, or fall back to net salary from
        // structure
        BigDecimal amountInr = request.getAmount();
        if (amountInr == null) {
            amountInr = resolveNetSalary(request.getPayrollId());
        }

        long amountPaise = amountInr.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        // Build Razorpay order
        JSONObject orderOptions = new JSONObject();
        orderOptions.put("amount", amountPaise);
        orderOptions.put("currency", "INR");
        orderOptions.put("receipt", "ORBIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        orderOptions.put("payment_capture", 1); // auto-capture

        Order razorpayOrder;
        try {
            razorpayOrder = razorpayClient.orders.create(orderOptions);
            // log.info("Razorpay order created: {}", razorpayOrder.get("id"));
        } catch (RazorpayException e) {
            // log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            throw new AppException(AppUtils.Status.RAZORPAY_ERROR, e.getMessage());
        }

        String razorpayOrderId = razorpayOrder.get("id");

        // Persist payment record
        PaymentEntity payment = PaymentEntity.builder()
                .employeePayroll(payroll)
                .razorpayOrderId(razorpayOrderId)
                .amountPaise(amountPaise)
                .amountInr(amountInr)
                .currency("INR")
                .description(request.getDescription() != null
                        ? request.getDescription()
                        : "Salary disbursement for " + payroll.getName())
                .status(AppUtils.PaymentStatus.CREATED)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment record saved id={}", payment.getId());

        return toOrderResponse(payment, payroll);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VERIFY PAYMENT
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse verifyPayment(PaymentVerifyRequest request) {
        log.info("Verifying payment for orderId={}", request.getRazorpayOrderId());

        PaymentEntity payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYMENT_NOT_FOUND));

        // Idempotency check
        if (payment.getStatus() == AppUtils.PaymentStatus.SUCCESS) {
            log.warn("Payment already verified for orderId={}", request.getRazorpayOrderId());
            throw new AppException(AppUtils.Status.PAYMENT_ALREADY_PROCESSED);
        }

        // Verify HMAC SHA256 signature
        boolean valid = verifySignature(
                request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                keySecret);

        if (!valid) {
            log.error("Invalid signature for orderId={}", request.getRazorpayOrderId());
            payment.setStatus(AppUtils.PaymentStatus.FAILED);
            payment.setFailureReason("Signature mismatch");
            paymentRepository.save(payment);
            throw new AppException(AppUtils.Status.INVALID_SIGNATURE);
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(AppUtils.PaymentStatus.SUCCESS);
        payment = paymentRepository.save(payment);

        log.info("Payment verified successfully orderId={} paymentId={}",
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        return toResponse(payment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEBHOOK
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        log.info("Received Razorpay webhook");

        // Verify webhook signature
        boolean valid = verifySignature(payload, signature, webhookSecret);
        if (!valid) {
            log.error("Webhook signature verification failed");
            throw new AppException(AppUtils.Status.INVALID_SIGNATURE);
        }

        JSONObject event;
        try {
            event = new JSONObject(payload);
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            throw new AppException(AppUtils.Status.RAZORPAY_ERROR, "Invalid webhook payload");
        }

        String eventType = event.optString("event", "");
        log.info("Processing webhook event: {}", eventType);

        JSONObject paymentData = event
                .optJSONObject("payload") != null
                        ? event.getJSONObject("payload")
                                .optJSONObject("payment") != null
                                        ? event.getJSONObject("payload").getJSONObject("payment")
                                                .optJSONObject("entity")
                                        : null
                        : null;

        if (paymentData == null) {
            log.warn("No payment entity in webhook payload, skipping");
            return;
        }

        String orderId = paymentData.optString("order_id", "");
        String razorpayPaymentId = paymentData.optString("id", "");

        paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
            switch (eventType) {
                case "payment.captured" -> {
                    if (payment.getStatus() != AppUtils.PaymentStatus.SUCCESS) {
                        payment.setStatus(AppUtils.PaymentStatus.SUCCESS);
                        payment.setRazorpayPaymentId(razorpayPaymentId);
                        paymentRepository.save(payment);
                        log.info("Webhook: payment captured orderId={}", orderId);
                    } else {
                        log.info("Webhook: payment already SUCCESS, skip idempotent update orderId={}", orderId);
                    }
                }
                case "payment.failed" -> {
                    String errorDesc = paymentData.optString("error_description", "Payment failed");
                    if (payment.getStatus() != AppUtils.PaymentStatus.FAILED) {
                        payment.setStatus(AppUtils.PaymentStatus.FAILED);
                        payment.setFailureReason(errorDesc);
                        paymentRepository.save(payment);
                        log.warn("Webhook: payment failed orderId={} reason={}", orderId, errorDesc);
                    }
                }
                default -> log.info("Webhook: unhandled event type={}", eventType);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REFUND
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse initiateRefund(UUID paymentId) {
        log.info("Initiating refund for paymentId={}", paymentId);

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != AppUtils.PaymentStatus.SUCCESS) {
            throw new AppException(AppUtils.Status.PAYMENT_FAILED);
        }

        JSONObject refundOptions = new JSONObject();
        refundOptions.put("amount", payment.getAmountPaise());

        try {
            razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundOptions);
            log.info("Razorpay refund initiated for paymentId={}", payment.getRazorpayPaymentId());
        } catch (RazorpayException e) {
            log.error("Razorpay refund failed: {}", e.getMessage(), e);
            throw new AppException(AppUtils.Status.RAZORPAY_ERROR, e.getMessage());
        }

        payment.setStatus(AppUtils.PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        return toResponse(payment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByPayroll(UUID payrollId) {
        return paymentRepository.findByEmployeePayroll_IdOrderByCreatedAtDesc(payrollId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByEmployee(UUID employeeId) {
        return paymentRepository.findByEmployeePayroll_Employee_IdOrderByCreatedAtDesc(employeeId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        return toResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYMENT_NOT_FOUND)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves net salary (basic + allowance - pf - tax - other_deduction) from
     * payroll salary structure.
     */
    private BigDecimal resolveNetSalary(UUID payrollId) {
        return salaryStructureRepository.findByEmployeePayroll_Id(payrollId)
                .map(s -> {
                    BigDecimal basic = nvl(s.getBasicSalary());
                    BigDecimal allowance = nvl(s.getAllowance());
                    BigDecimal pf = nvl(s.getPf());
                    BigDecimal tax = nvl(s.getTax());
                    BigDecimal other = nvl(s.getOtherDeduction());
                    return basic.add(allowance).subtract(pf).subtract(tax).subtract(other);
                })
                .filter(amt -> amt.compareTo(BigDecimal.ONE) >= 0)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * HMAC SHA256 signature verification. Used for both payment verification and
     * webhooks.
     */
    private boolean verifySignature(String data, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Signature computation failed", e);
            return false;
        }
    }

    private PaymentOrderResponse toOrderResponse(PaymentEntity p, EmployeePayrollEntity payroll) {
        return PaymentOrderResponse.builder()
                .id(p.getId())
                .payrollId(payroll.getId())
                .employeeName(payroll.getName())
                .employeeEmail(payroll.getEmail())
                .razorpayOrderId(p.getRazorpayOrderId())
                .amountInr(p.getAmountInr())
                .amountPaise(p.getAmountPaise())
                .currency(p.getCurrency())
                .description(p.getDescription())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PaymentResponse toResponse(PaymentEntity p) {
        EmployeePayrollEntity payroll = p.getEmployeePayroll();
        return PaymentResponse.builder()
                .id(p.getId())
                .payrollId(payroll != null ? payroll.getId() : null)
                .employeeName(payroll != null ? payroll.getName() : null)
                .employeeEmail(payroll != null ? payroll.getEmail() : null)
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .amountInr(p.getAmountInr())
                .currency(p.getCurrency())
                .description(p.getDescription())
                .status(p.getStatus())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}

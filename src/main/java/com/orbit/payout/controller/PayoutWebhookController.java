package com.orbit.payout.controller;

import com.orbit.exception.AppException;
import com.orbit.payout.service.PayoutService;
import com.orbit.util.AppUtils;
import com.razorpay.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payouts/webhook")
@RequiredArgsConstructor
@Tag(name = "Payout Webhook", description = "Razorpay Webhook for Payout Status Updates")
public class PayoutWebhookController {

    private final PayoutService payoutService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    @Operation(summary = "Handle incoming Razorpay Payout Webhooks")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payloadBody,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Received Razorpay Payout Webhook -> Signature: {}", signature);

        try {
            boolean isSignatureValid = Utils.verifyWebhookSignature(payloadBody, signature, webhookSecret);
            if (!isSignatureValid) {
                log.error("Webhook signature verification failed.");
                throw new AppException(AppUtils.Status.WEBHOOK_SIGNATURE_INVALID);
            }

            payoutService.handleWebhook(payloadBody, signature);
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing Payout Webhook", e);
            throw new AppException(AppUtils.Status.RAZORPAY_ERROR, e.getMessage());
        }
    }
}

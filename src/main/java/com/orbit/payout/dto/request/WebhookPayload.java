package com.orbit.payout.dto.request;

import lombok.Data;

@Data
public class WebhookPayload {
    private String event;
    private WebhookPayloadData payload;

    @Data
    public static class WebhookPayloadData {
        private WebhookPayout payout;
    }

    @Data
    public static class WebhookPayout {
        private WebhookPayoutEntity entity;
    }

    @Data
    public static class WebhookPayoutEntity {
        private String id;
        private String fund_account_id;
        private Long amount;
        private String status;
        private String reference_id;
        private String mode;
        private String narration;
        private String failure_reason;
    }
}

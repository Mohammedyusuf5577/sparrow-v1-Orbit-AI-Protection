package com.orbit.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.config.WhatsAppConfig;
import com.orbit.entity.NotificationOutboxEntity;
import com.orbit.repository.NotificationOutboxRepository;
import com.orbit.service.OutboxWorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxWorkerServiceImpl implements OutboxWorkerService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final WhatsAppConfig whatsAppConfig;
    private final ObjectMapper objectMapper;

    @Qualifier("whatsappRestTemplate")
    private final RestTemplate restTemplate;

    @Override
    public void processOutboxBatch() {
        List<NotificationOutboxEntity> pendingRecords = fetchPendingBatch();
        if (pendingRecords.isEmpty()) {
            return;
        }

        log.debug("Processing {} notification outbox records", pendingRecords.size());

        for (NotificationOutboxEntity record : pendingRecords) {
            try {
                processSingleRecord(record);
            } catch (Exception e) {
                log.error("Unexpected error processing outbox record ID {}", record.getId(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationOutboxEntity> fetchPendingBatch() {
        return notificationOutboxRepository.findPendingForProcessing(LocalDateTime.now());
    }

    private void processSingleRecord(NotificationOutboxEntity record) {
        if (!whatsAppConfig.isConfigured()) {
            log.info("[WhatsApp SIMULATION] Meta API credentials not set. Simulated notification to phone='{}', template='{}', payload={}",
                    record.getRecipientPhone(), record.getTemplateName(), record.getPayload());
            markRecordSent(record.getId());
            return;
        }

        // Call Meta Graph API outside transaction
        try {
            sendMetaWhatsAppMessage(record);
            markRecordSent(record.getId());
            log.info("WhatsApp notification sent successfully for outbox record ID {}", record.getId());
        } catch (HttpStatusCodeException httpEx) {
            handleHttpException(record, httpEx);
        } catch (Exception ex) {
            handleGeneralException(record, ex);
        }
    }

    private void sendMetaWhatsAppMessage(NotificationOutboxEntity record) throws Exception {
        String url = String.format("https://graph.facebook.com/%s/%s/messages",
                whatsAppConfig.getApiVersion(), whatsAppConfig.getPhoneNumberId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsAppConfig.getToken());

        Map<String, String> payloadMap = Collections.emptyMap();
        try {
            payloadMap = objectMapper.readValue(record.getPayload(), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse outbox record payload JSON: {}", record.getPayload());
        }

        List<Map<String, String>> bodyParameters = new ArrayList<>();
        bodyParameters.add(Map.of("type", "text", "text", payloadMap.getOrDefault("employeeName", "Team Member")));
        bodyParameters.add(Map.of("type", "text", "text", payloadMap.getOrDefault("projectName", "")));
        bodyParameters.add(Map.of("type", "text", "text", payloadMap.getOrDefault("clientName", "")));
        bodyParameters.add(Map.of("type", "text", "text", payloadMap.getOrDefault("startDate", "")));
        bodyParameters.add(Map.of("type", "text", "text", payloadMap.getOrDefault("endDate", "")));

        Map<String, Object> templateMap = new HashMap<>();
        templateMap.put("name", record.getTemplateName());
        templateMap.put("language", Map.of("code", whatsAppConfig.getTemplateLang()));
        templateMap.put("components", List.of(Map.of("type", "body", "parameters", bodyParameters)));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("recipient_type", "individual");
        requestBody.put("to", record.getRecipientPhone());
        requestBody.put("type", "template");
        requestBody.put("template", templateMap);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Meta API returned non-2xx status: " + response.getStatusCode());
        }
    }

    private void handleHttpException(NotificationOutboxEntity record, HttpStatusCodeException httpEx) {
        String errorMsg = String.format("Meta API error [%s]: %s", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
        log.error("WhatsApp API HTTP error for outbox record {}: {}", record.getId(), errorMsg);

        if (httpEx.getStatusCode().is4xxClientError()) {
            // Client error (invalid number, bad template, bad auth) -> Do not retry
            markRecordFailed(record.getId(), errorMsg);
        } else {
            // 5xx Server error -> Retry with exponential backoff
            scheduleRetryOrMarkFailed(record, errorMsg);
        }
    }

    private void handleGeneralException(NotificationOutboxEntity record, Exception ex) {
        String errorMsg = "WhatsApp dispatch failure: " + ex.getMessage();
        log.error("Unexpected error for outbox record {}: {}", record.getId(), errorMsg);
        scheduleRetryOrMarkFailed(record, errorMsg);
    }

    private void scheduleRetryOrMarkFailed(NotificationOutboxEntity record, String errorMsg) {
        int nextAttempt = record.getAttempts() + 1;
        if (nextAttempt >= record.getMaxAttempts()) {
            markRecordFailed(record.getId(), "Max retry attempts reached. " + errorMsg);
        } else {
            long delaySeconds = (long) Math.pow(2, nextAttempt) * 30L;
            LocalDateTime nextRetry = LocalDateTime.now().plusSeconds(delaySeconds);
            updateRecordRetry(record.getId(), nextRetry, errorMsg);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRecordSent(UUID id) {
        notificationOutboxRepository.markSent(id, LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRecordFailed(UUID id, String errorLog) {
        notificationOutboxRepository.updateFailure(id, "FAILED", null, errorLog, LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRecordRetry(UUID id, LocalDateTime nextRetryAt, String errorLog) {
        notificationOutboxRepository.updateFailure(id, "PENDING", nextRetryAt, errorLog, LocalDateTime.now());
    }
}

package com.orbit.payout.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.exception.AppException;
import com.orbit.entity.UserEntity;
import com.orbit.repository.UserRepository;
import com.orbit.payroll.entity.EmployeePayrollEntity;
import com.orbit.payroll.entity.PayrollBankDetailsEntity;
import com.orbit.payroll.repository.EmployeePayrollRepository;
import com.orbit.payroll.repository.PayrollBankDetailsRepository;
import com.orbit.payout.dto.request.CreatePayoutRecordRequest;
import com.orbit.payout.dto.request.RegisterBankDetailsRequest;
import com.orbit.payout.dto.request.WebhookPayload;
import com.orbit.payout.dto.response.BankRegistrationResponse;
import com.orbit.payout.dto.response.PayoutRecordResponse;
import com.orbit.payout.dto.response.PayoutTransactionResponse;
import com.orbit.payout.entity.PayoutAuditLogEntity;
import com.orbit.payout.entity.PayoutRecordEntity;
import com.orbit.payout.entity.PayoutTransactionEntity;
import com.orbit.payout.repository.PayoutAuditLogRepository;
import com.orbit.payout.repository.PayoutRecordRepository;
import com.orbit.payout.repository.PayoutTransactionRepository;
import com.orbit.payout.service.PayoutService;
import com.orbit.payout.util.EncryptionUtil;
import com.orbit.util.AppUtils;
import com.razorpay.FundAccount;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutServiceImpl implements PayoutService {

    private final RazorpayClient razorpayClient;
    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;
    private final EmployeePayrollRepository employeePayrollRepository;
    private final PayrollBankDetailsRepository bankDetailsRepository;
    private final PayoutRecordRepository payoutRecordRepository;
    private final PayoutTransactionRepository payoutTransactionRepository;
    private final PayoutAuditLogRepository auditLogRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.account.number}")
    private String merchantAccountNumber;

    @Value("${razorpay.payout.mode}")
    private String defaultPayoutMode;

    @Value("${razorpay.payout.purpose}")
    private String defaultPayoutPurpose;

    @Value("${razorpay.contact.type}")
    private String defaultContactType;

    @Override
    @Transactional
    public BankRegistrationResponse registerBankDetails(RegisterBankDetailsRequest request) {
        EmployeePayrollEntity payroll = employeePayrollRepository.findById(request.getEmployeePayrollId())
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));

        PayrollBankDetailsEntity bankDetails = payroll.getBankDetails();
        if (bankDetails == null) {
            bankDetails = PayrollBankDetailsEntity.builder()
                    .employeePayroll(payroll)
                    .retryCount(0)
                    .build();
        }

        bankDetails.setAccountName(request.getAccountName());
        bankDetails.setBankName(request.getBankName());
        bankDetails.setAccountNumber(encryptionUtil.encrypt(request.getAccountNumber()));
        bankDetails.setIfsc(request.getIfsc());
        bankDetails.setUpiId(request.getUpiId());

        try {
            // 1. Create OR Update Contact
            if (bankDetails.getRazorpayContactId() == null) {
                JSONObject contactReq = new JSONObject();
                contactReq.put("name", request.getAccountName());
                contactReq.put("email",
                        payroll.getEmail() != null ? payroll.getEmail() : payroll.getEmployee().getEmail());
                contactReq.put("contact", payroll.getMobile() != null ? payroll.getMobile() : "9999999999"); // Mock
                                                                                                             // default
                                                                                                             // if null
                contactReq.put("type", defaultContactType);
                contactReq.put("reference_id", payroll.getId().toString());

                JSONObject contact = razorpayApiPost("https://api.razorpay.com/v1/contacts", contactReq);
                bankDetails.setRazorpayContactId(contact.getString("id"));

                logAudit("CONTACT_CREATED", payroll.getEmployee().getId(), "EMPLOYEE",
                        "Razorpay contact created: " + contact.getString("id"));
            }

            // 2. Create Fund Account
            if (bankDetails.getRazorpayFundAccountId() == null) {
                JSONObject fundAccountReq = new JSONObject();
                fundAccountReq.put("contact_id", bankDetails.getRazorpayContactId());
                fundAccountReq.put("account_type", "bank_account");

                JSONObject bankAccount = new JSONObject();
                bankAccount.put("name", request.getAccountName());
                bankAccount.put("ifsc", request.getIfsc());
                bankAccount.put("account_number", request.getAccountNumber());
                fundAccountReq.put("bank_account", bankAccount);

                FundAccount fundAccount = razorpayClient.fundAccount.create(fundAccountReq);
                bankDetails.setRazorpayFundAccountId(fundAccount.get("id"));

                logAudit("FUND_ACCOUNT_CREATED", payroll.getEmployee().getId(), "EMPLOYEE",
                        "Razorpay fund account created: " + fundAccount.get("id"));
            }
        } catch (RazorpayException e) {
            log.error("Failed to register bank details with Razorpay", e);
            throw new AppException(AppUtils.Status.RAZORPAY_ERROR, e.getMessage());
        }

        bankDetails = bankDetailsRepository.save(bankDetails);

        BankRegistrationResponse response = new BankRegistrationResponse();
        response.setPayrollId(payroll.getId());
        response.setAccountName(bankDetails.getAccountName());
        response.setBankName(bankDetails.getBankName());
        response.setMaskedAccountNumber(
                "****" + request.getAccountNumber().substring(Math.max(0, request.getAccountNumber().length() - 4)));
        response.setIfsc(bankDetails.getIfsc());
        response.setUpiId(bankDetails.getUpiId());
        response.setRazorpayContactId(bankDetails.getRazorpayContactId());
        response.setRazorpayFundAccountId(bankDetails.getRazorpayFundAccountId());
        response.setRegisteredWithRazorpay(bankDetails.getRazorpayFundAccountId() != null);

        return response;
    }

    @Override
    @Transactional
    public PayoutRecordResponse createPayoutRecord(CreatePayoutRecordRequest request) {
        UserEntity user = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(AppUtils.Status.USER_NOT_FOUND));

        if (payoutRecordRepository.existsByUserIdAndSalaryDate(user.getId(), request.getSalaryDate())) {
            throw new AppException(AppUtils.Status.VALIDATION_FAILED,
                    "Payout record for this date already exists for employee");
        }

        PayoutRecordEntity record = PayoutRecordEntity.builder()
                .user(user)
                .salaryAmount(request.getSalaryAmount())
                .salaryDate(request.getSalaryDate())
                .status(AppUtils.PayoutStatus.PENDING)
                .retryCount(0)
                .build();

        record = payoutRecordRepository.save(record);
        logAudit("PAYOUT_RECORD_CREATED", record.getId(), "PAYOUT_RECORD",
                "Salary payout scheduled for " + request.getSalaryDate());

        return mapRecord(record);
    }

    @Override
    public List<PayoutRecordResponse> getAllPayoutRecords() {
        return payoutRecordRepository.findAll().stream()
                .map(this::mapRecord)
                .collect(Collectors.toList());
    }

    @Override
    public List<PayoutTransactionResponse> getAllPayoutTransactions() {
        return payoutTransactionRepository.findAll().stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PayoutTransactionResponse processPayout(UUID employeeId) {
        // Find today's pending payout record for the employee
        List<PayoutRecordEntity> pendingRecords = payoutRecordRepository.findByUserId(employeeId).stream()
                .filter(p -> p.getSalaryDate().isEqual(LocalDate.now()) || p.getSalaryDate().isBefore(LocalDate.now()))
                .filter(p -> p.getStatus() == AppUtils.PayoutStatus.PENDING
                        || p.getStatus() == AppUtils.PayoutStatus.FAILED)
                .collect(Collectors.toList());

        if (pendingRecords.isEmpty()) {
            throw new AppException(AppUtils.Status.PAYOUT_RECORD_NOT_FOUND,
                    "No valid pending payout record found for today");
        }

        return processPayoutByRecordId(pendingRecords.get(0).getId());
    }

    @Override
    @Transactional
    public PayoutTransactionResponse processPayoutByRecordId(UUID recordId) {
        PayoutRecordEntity record = payoutRecordRepository.findById(recordId)
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYOUT_RECORD_NOT_FOUND));

        if (record.getStatus() == AppUtils.PayoutStatus.SUCCESS
                || record.getStatus() == AppUtils.PayoutStatus.PROCESSING) {
            throw new AppException(AppUtils.Status.PAYOUT_ALREADY_PROCESSED);
        }

        EmployeePayrollEntity payroll = employeePayrollRepository.findByEmployeeId(record.getUser().getId())
                .orElseThrow(() -> new AppException(AppUtils.Status.PAYROLL_NOT_FOUND));

        PayrollBankDetailsEntity bankDetails = payroll.getBankDetails();
        if (bankDetails == null || bankDetails.getRazorpayFundAccountId() == null) {
            throw new AppException(AppUtils.Status.BANK_DETAILS_NOT_FOUND);
        }

        PayoutTransactionEntity transaction = PayoutTransactionEntity.builder()
                .user(record.getUser())
                .payoutRecord(record)
                .amount(record.getSalaryAmount())
                .mode(AppUtils.PayoutMode.valueOf(defaultPayoutMode))
                .status(AppUtils.PayoutStatus.PROCESSING)
                .fundAccountId(bankDetails.getRazorpayFundAccountId())
                .narration("Salary payout for " + record.getSalaryDate())
                .referenceId(record.getId().toString() + "-" + System.currentTimeMillis())
                .retryCount(0)
                .build();

        transaction = payoutTransactionRepository.save(transaction);
        record.setStatus(AppUtils.PayoutStatus.PROCESSING);
        payoutRecordRepository.save(record);

        try {
            JSONObject payoutReq = new JSONObject();
            payoutReq.put("account_number", merchantAccountNumber);
            payoutReq.put("fund_account_id", bankDetails.getRazorpayFundAccountId());
            payoutReq.put("amount", record.getSalaryAmount().multiply(new BigDecimal("100")).longValue());
            payoutReq.put("currency", "INR");
            payoutReq.put("mode", defaultPayoutMode);
            payoutReq.put("purpose", defaultPayoutPurpose);
            payoutReq.put("queue_if_low_balance", true);
            payoutReq.put("reference_id", transaction.getReferenceId());
            payoutReq.put("narration", transaction.getNarration());

            JSONObject rzpPayout = razorpayApiPost("https://api.razorpay.com/v1/payouts", payoutReq);

            transaction.setRazorpayPayoutId(rzpPayout.getString("id"));
            String status = rzpPayout.getString("status");

            if ("processed".equalsIgnoreCase(status)) {
                transaction.setStatus(AppUtils.PayoutStatus.SUCCESS);
                record.setStatus(AppUtils.PayoutStatus.SUCCESS);
            } else if ("rejected".equalsIgnoreCase(status)) {
                transaction.setStatus(AppUtils.PayoutStatus.FAILED);
                record.setStatus(AppUtils.PayoutStatus.FAILED);
                transaction.setFailureReason(rzpPayout.optString("status_details_reason"));
            } else {
                // Pending / queued / processing
                transaction.setStatus(AppUtils.PayoutStatus.PROCESSING);
            }
            logAudit("PAYOUT_INITIATED", transaction.getId(), "PAYOUT_TRANSACTION",
                    "Payout status from Razorpay: " + status);

        } catch (RazorpayException e) {
            log.error("Failed to process payout via Razorpay", e);
            transaction.setStatus(AppUtils.PayoutStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            record.setStatus(AppUtils.PayoutStatus.FAILED);
            record.setRetryCount(record.getRetryCount() + 1);

            logAudit("PAYOUT_FAILED", transaction.getId(), "PAYOUT_TRANSACTION", e.getMessage());
        }

        payoutTransactionRepository.save(transaction);
        payoutRecordRepository.save(record);

        return mapTransaction(transaction);
    }

    @Override
    @Transactional
    public void processAllPendingPayoutsForToday() {
        List<PayoutRecordEntity> pendingRecords = payoutRecordRepository.findPendingPayoutsUpToDate(LocalDate.now());
        for (PayoutRecordEntity record : pendingRecords) {
            try {
                processPayoutByRecordId(record.getId());
            } catch (Exception e) {
                log.error("Failed to process payout for record: {}", record.getId(), e);
            }
        }
    }

    @Override
    public List<PayoutTransactionResponse> getPayoutHistoryByEmployee(UUID employeeId) {
        return payoutTransactionRepository.findByUserId(employeeId).stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleWebhook(String payloadBody, String signature) {
        // Handled directly inside WebhookController to pass correct raw string
        // We will receive valid deserialized payload from the webhook controller
        try {
            WebhookPayload payload = objectMapper.readValue(payloadBody, WebhookPayload.class);
            if (payload == null || payload.getPayload() == null || payload.getPayload().getPayout() == null) {
                return;
            }

            WebhookPayload.WebhookPayoutEntity rzpPayout = payload.getPayload().getPayout().getEntity();
            String payoutId = rzpPayout.getId();
            String event = payload.getEvent();

            Optional<PayoutTransactionEntity> transactionOpt = payoutTransactionRepository
                    .findByRazorpayPayoutId(payoutId);
            if (transactionOpt.isEmpty()) {
                log.warn("Received webhook for unknown payout ID: {}", payoutId);
                return;
            }

            PayoutTransactionEntity transaction = transactionOpt.get();
            PayoutRecordEntity record = transaction.getPayoutRecord();

            logAudit("WEBHOOK_RECEIVED", transaction.getId(), "WEBHOOK", "Event: " + event);

            switch (event) {
                case "payout.processed":
                    transaction.setStatus(AppUtils.PayoutStatus.SUCCESS);
                    record.setStatus(AppUtils.PayoutStatus.SUCCESS);
                    break;
                case "payout.failed":
                case "payout.reversed":
                case "payout.rejected":
                    transaction.setStatus(AppUtils.PayoutStatus.FAILED);
                    record.setStatus(AppUtils.PayoutStatus.FAILED);
                    transaction.setFailureReason(rzpPayout.getFailure_reason());
                    break;
                case "payout.queued":
                case "payout.initiated":
                    transaction.setStatus(AppUtils.PayoutStatus.PROCESSING);
                    record.setStatus(AppUtils.PayoutStatus.PROCESSING);
                    break;
            }

            payoutTransactionRepository.save(transaction);
            payoutRecordRepository.save(record);

        } catch (Exception e) {
            log.error("Error processing Razorpay webhook", e);
        }
    }

    private void logAudit(String eventType, UUID entityId, String entityType, String description) {
        PayoutAuditLogEntity log = PayoutAuditLogEntity.builder()
                .eventType(eventType)
                .entityId(entityId)
                .entityType(entityType)
                .userId(AppUtils.getPrincipalUserId())
                .description(description)
                .build();
        auditLogRepository.save(log);
    }

    private PayoutRecordResponse mapRecord(PayoutRecordEntity entity) {
        PayoutRecordResponse dto = new PayoutRecordResponse();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getUser().getId());
        dto.setEmployeeName(entity.getUser().getFullName());
        dto.setEmployeeEmail(entity.getUser().getEmail());
        dto.setSalaryAmount(entity.getSalaryAmount());
        dto.setSalaryDate(entity.getSalaryDate());
        dto.setStatus(entity.getStatus());
        dto.setRetryCount(entity.getRetryCount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private PayoutTransactionResponse mapTransaction(PayoutTransactionEntity entity) {
        PayoutTransactionResponse dto = new PayoutTransactionResponse();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getUser().getId());
        dto.setEmployeeName(entity.getUser().getFullName());
        dto.setPayoutRecordId(entity.getPayoutRecord().getId());
        dto.setRazorpayPayoutId(entity.getRazorpayPayoutId());
        dto.setAmount(entity.getAmount());
        dto.setMode(entity.getMode());
        dto.setStatus(entity.getStatus());
        dto.setReferenceId(entity.getReferenceId());
        dto.setFundAccountId(entity.getFundAccountId());
        dto.setNarration(entity.getNarration());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setFailureReason(entity.getFailureReason());
        dto.setRetryCount(entity.getRetryCount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private JSONObject razorpayApiPost(String url, JSONObject payload) throws RazorpayException {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBasicAuth(razorpayKeyId, razorpayKeySecret);
            org.springframework.http.HttpEntity<String> requestEntity = new org.springframework.http.HttpEntity<>(payload.toString(), headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            return new JSONObject(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new RazorpayException("Razorpay API Error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RazorpayException("Internal error while calling Razorpay: " + e.getMessage());
        }
    }
}

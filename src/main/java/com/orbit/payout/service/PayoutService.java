package com.orbit.payout.service;

import com.orbit.payout.dto.request.CreatePayoutRecordRequest;
import com.orbit.payout.dto.request.RegisterBankDetailsRequest;
import com.orbit.payout.dto.response.BankRegistrationResponse;
import com.orbit.payout.dto.response.PayoutRecordResponse;
import com.orbit.payout.dto.response.PayoutTransactionResponse;

import java.util.List;
import java.util.UUID;

public interface PayoutService {

    BankRegistrationResponse registerBankDetails(RegisterBankDetailsRequest request);

    PayoutRecordResponse createPayoutRecord(CreatePayoutRecordRequest request);

    List<PayoutRecordResponse> getAllPayoutRecords();

    List<PayoutTransactionResponse> getAllPayoutTransactions();

    PayoutTransactionResponse processPayout(UUID employeeId);
    
    PayoutTransactionResponse processPayoutByRecordId(UUID recordId);

    void processAllPendingPayoutsForToday();

    List<PayoutTransactionResponse> getPayoutHistoryByEmployee(UUID employeeId);

    void handleWebhook(String payload, String signature);
}

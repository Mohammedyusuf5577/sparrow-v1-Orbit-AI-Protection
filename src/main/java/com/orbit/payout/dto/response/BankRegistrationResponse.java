package com.orbit.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankRegistrationResponse {
    private UUID payrollId;
    private String accountName;
    private String bankName;
    private String maskedAccountNumber;
    private String ifsc;
    private String upiId;
    private String razorpayContactId;
    private String razorpayFundAccountId;
    private boolean registeredWithRazorpay;
}

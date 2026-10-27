package com.example.creditworthinesschecker.financing.dto;
import com.example.creditworthinesschecker.credit.enums.BanCheckStatus;
import com.example.creditworthinesschecker.financing.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.financing.enums.LoanType;

import java.time.Instant;
import java.util.UUID;

public record FinancingRequestResponse(
        UUID id,
        String personIdCode,
        LoanType loanType,
        LoanPurposeOfUse loanPurposeOfUse,
        Instant submittedAt,
        BanCheckStatus banCheckStatus,
        Boolean voluntaryBanInEffect
) { }

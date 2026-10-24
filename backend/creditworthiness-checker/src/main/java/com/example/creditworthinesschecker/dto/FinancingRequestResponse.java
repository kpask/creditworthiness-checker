package com.example.creditworthinesschecker.dto;
import com.example.creditworthinesschecker.enums.BanCheckStatus;
import com.example.creditworthinesschecker.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.enums.LoanType;

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

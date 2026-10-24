package com.example.creditworthinesschecker.dto;
import com.example.creditworthinesschecker.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.enums.LoanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FinancingRequestRequest(
        @NotBlank String personIdCode,
        @NotNull LoanType loanType,
        @NotNull LoanPurposeOfUse loanPurposeOfUse
) {}

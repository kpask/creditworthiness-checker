package com.example.creditworthinesschecker.financing.dto;
import com.example.creditworthinesschecker.financing.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.financing.enums.LoanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FinancingRequestRequest(
        @NotBlank String personIdCode,
        @NotNull LoanType loanType,
        @NotNull LoanPurposeOfUse loanPurposeOfUse
) {}

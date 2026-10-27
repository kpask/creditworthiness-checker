package com.example.creditworthinesschecker.credit.dto;

import com.example.creditworthinesschecker.credit.enums.CreditRegisterExtractPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreditExtractRequest(
        @NotBlank String personalIdCode,
        @NotNull CreditRegisterExtractPurpose extractPurpose
) {}

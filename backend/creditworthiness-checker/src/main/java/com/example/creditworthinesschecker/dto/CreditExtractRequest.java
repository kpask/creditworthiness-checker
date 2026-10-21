package com.example.creditworthinesschecker.dto;

import com.example.creditworthinesschecker.enums.CreditRegisterExtractPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreditExtractRequest(
        @NotBlank String personalIdCode,
        @NotNull CreditRegisterExtractPurpose extractPurpose
) {}

package com.example.creditworthinesschecker.dto;

import com.example.creditworthinesschecker.enums.Country;
import com.example.creditworthinesschecker.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.enums.IdCodeType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PcrCreditExtractRequest(
        @NotNull IdCodeType idCodeType,
        @NotEmpty String requesterIdCode,
        @NotNull Country requesterCountry,
        @NotNull IdCodeType requestedIdCodeType,
        @NotEmpty String requestedIdCode,
        @NotNull CreditRegisterExtractPurpose extractPurpose
) {}

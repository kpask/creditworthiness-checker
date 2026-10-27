package com.example.creditworthinesschecker.credit.dto;

import com.example.creditworthinesschecker.credit.enums.Country;
import com.example.creditworthinesschecker.credit.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.credit.enums.IdCodeType;
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

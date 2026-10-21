package com.example.creditworthinesschecker.dto;

import com.example.creditworthinesschecker.enums.Country;
import com.example.creditworthinesschecker.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.enums.IdCodeType;
import jakarta.validation.constraints.NotEmpty;

public record PcrCreditExtractRequest(
        @NotEmpty IdCodeType idCodeType,
        @NotEmpty String requesterIdCode,
        @NotEmpty Country requesterCountry,
        @NotEmpty IdCodeType requestedIdCodeType,
        @NotEmpty String requestedIdCode,
        @NotEmpty CreditRegisterExtractPurpose extractPurpose
) {}

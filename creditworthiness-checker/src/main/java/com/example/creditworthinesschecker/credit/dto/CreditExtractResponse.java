package com.example.creditworthinesschecker.credit.dto;

public record CreditExtractResponse(
        PcrCreditRegisterExtractResponse extract,
        boolean historical
) {}

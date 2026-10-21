package com.example.creditworthinesschecker.dto;

import java.time.Instant;

public record CreditExtractResponse(
        PcrCreditRegisterExtractResponse extract,
        boolean historical
) {}

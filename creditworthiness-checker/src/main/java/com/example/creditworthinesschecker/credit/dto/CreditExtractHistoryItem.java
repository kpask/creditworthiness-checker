package com.example.creditworthinesschecker.credit.dto;

import java.time.Instant;
import java.util.UUID;

public record CreditExtractHistoryItem(
        UUID id,
        UUID extractReference,
        Instant fetchedAt,
        Boolean voluntaryBanInEffect
){}
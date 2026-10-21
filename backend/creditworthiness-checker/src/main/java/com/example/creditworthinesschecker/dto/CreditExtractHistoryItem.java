package com.example.creditworthinesschecker.dto;

import java.time.Instant;
import java.util.UUID;

public record CreditExtractHistoryItem(
        UUID id,
        UUID extractReference,
        Instant fetchedAt,
        Boolean voluntaryBanInEffect
){}
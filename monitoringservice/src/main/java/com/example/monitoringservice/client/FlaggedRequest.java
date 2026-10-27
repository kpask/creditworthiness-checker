package com.example.monitoringservice.client;

import java.time.Instant;
import java.util.UUID;

public record FlaggedRequest(
        UUID id,
        String personIdCode,
        String loanType,
        String loanPurposeOfUse,
        Instant submittedAt,
        String banCheckStatus,
        Boolean voluntaryBanInEffect
) {}

package com.example.creditworthinesschecker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PcrErrorResponse(
        String statusMessage,
        UUID correlationId,
        List<ErrorItem> errorResponses
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorItem(
            String fieldName,
            String errorCode,
            String errorDescription
    ) {}
}

package com.example.monitoringservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

@Entity
public class ReportedRequest {
    @Id
    private UUID requestId;
    private Instant reportedAt;

    protected ReportedRequest() {}

    public ReportedRequest(UUID requestId, Instant reportedAt) {
        this.requestId = requestId;
        this.reportedAt = reportedAt;
    }
    public UUID getRequestId() { return requestId; }
    public Instant getReportedAt() { return reportedAt; }
}

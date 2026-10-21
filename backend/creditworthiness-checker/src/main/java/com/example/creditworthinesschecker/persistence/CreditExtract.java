package com.example.creditworthinesschecker.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(indexes = {
        @Index(name = "idx_credit_extract_person_fetched", columnList = "personIdCode, fetchedAt"),
        @Index(name = "idx_credit_extract_reference", columnList = "extractReference")
})
public class CreditExtract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String personIdCode;

    @Column(nullable = false)
    private UUID extractReference;

    @Column(nullable = false)
    private Instant fetchedAt;

    private Boolean voluntaryBanInEffect;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode responseData;

    protected CreditExtract() {}
    public CreditExtract(String personIdCode, UUID extractReference, Instant fetchedAt, Boolean voluntaryBanInEffect, JsonNode responseData) {
        this.personIdCode = personIdCode;
        this.extractReference = extractReference;
        this.fetchedAt = fetchedAt;
        this.voluntaryBanInEffect = voluntaryBanInEffect;
        this.responseData = responseData;
    }

    public UUID getId() {
        return id;
    }

    public String getPersonIdCode() {
        return personIdCode;
    }

    public UUID getExtractReference() {
        return extractReference;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public Boolean getVoluntaryBanInEffect() {
        return voluntaryBanInEffect;
    }

    public JsonNode getResponseData() {
        return responseData;
    }
}

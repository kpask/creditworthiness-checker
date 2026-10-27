
package com.example.creditworthinesschecker.financing;

import com.example.creditworthinesschecker.credit.enums.BanCheckStatus;
import com.example.creditworthinesschecker.financing.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.financing.enums.LoanType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class FinancingRequest {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String personIdCode;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    private LoanPurposeOfUse loanPurposeOfUse;

    @Column(nullable = false)
    private Instant submittedAt;

    private Boolean voluntaryBanInEffect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BanCheckStatus banCheckStatus;

    protected FinancingRequest() {}

    public FinancingRequest(String personIdCode, LoanType loanType, LoanPurposeOfUse loanPurposeOfUse) {
        this.personIdCode = personIdCode;
        this.loanType = loanType;
        this.loanPurposeOfUse = loanPurposeOfUse;
        this.submittedAt = Instant.now();
        this.voluntaryBanInEffect = null;
        this.banCheckStatus = BanCheckStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public String getPersonIdCode() {
        return personIdCode;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public LoanPurposeOfUse getLoanPurposeOfUse() {
        return loanPurposeOfUse;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Boolean getVoluntaryBanInEffect() {
        return voluntaryBanInEffect;
    }

    public BanCheckStatus getBanCheckStatus() {
        return banCheckStatus;
    }

    public void setVoluntaryBanInEffect(Boolean voluntaryBanInEffect) {
        this.voluntaryBanInEffect = voluntaryBanInEffect;
    }

    public void setBanCheckStatus(BanCheckStatus banCheckStatus) {
        this.banCheckStatus = banCheckStatus;
    }
}

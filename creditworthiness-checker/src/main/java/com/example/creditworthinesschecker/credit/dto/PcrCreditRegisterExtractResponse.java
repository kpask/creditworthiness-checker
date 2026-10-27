package com.example.creditworthinesschecker.credit.dto;

import com.example.creditworthinesschecker.credit.enums.CollateralType;
import com.example.creditworthinesschecker.credit.enums.IdCodeType;
import com.example.creditworthinesschecker.credit.enums.ReasonForCreditBan;
import com.example.creditworthinesschecker.credit.enums.RepaymentMethod;
import com.example.creditworthinesschecker.financing.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.financing.enums.LoanType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PcrCreditRegisterExtractResponse(
        String statusMessage,
        ExtractData creditRegisterExtract
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExtractData(
            UUID extractReference,
            Instant creationTimeUtc,
            PersonRequested personRequested,
            Requester owner,
            VoluntaryBanOnCredits voluntaryBanOnCredits,
            CreditInformationSummary creditInformationSummary,
            List<Loan> loans,
            List<IncomeData> incomeData,
            DeceasedPerson deceasedPerson
    ) {}

    public record PersonRequested(
            IdCodeType idCodeType,
            String idCode
    ) {}

    public record Requester(
            IdCodeType idCodeType,
            String idCode,
            String countryCode
    ) {}

    public record VoluntaryBanOnCredits(
            Boolean isInEffect,
            ReasonForCreditBan reason
    ) {}

    public record CreditInformationSummary(
            Integer lendersCount,
            Integer loanContractsCount,
            Integer guaranteedLoanContractsCount,
            List<CurrencyAmount> repaymentsPaidLastAmount,
            List<CurrencyAmount> sumOfMonthlyLeasingInstalments
    ) {}

    public record CurrencyAmount(
            String currencyCode,
            BigDecimal sum
    ) {}

    public record Loan(
            LoanType loanType,
            LocalDate contractDate,
            Boolean isLoanWithCollateral,
            List<CollateralType> collateralType,
            Integer borrowersCount,
            String currencyCode,
            BusinessInformation businessInformation,
            List<DefermentPeriod> defermentPeriods,
            PaymentPlan paymentPlan,
            LumpSumLoan lumpSumLoan,
            RunningAccountLoan runningAccountLoan,
            LeasingContract leasingContract,
            List<DelayedAmount> delayedAmount
    ) {}

    public record BusinessInformation(
            String borrowerBusinessID,
            String borrowerBusinessName
    ) {}

    public record DefermentPeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record PaymentPlan(
            Boolean isInDebtArrangement,
            Boolean isInBusinessRestructuringProgram,
            Boolean accuracyIsDenied
    ) {}

    public record LumpSumLoan(
            RepaymentMethod repaymentMethod,
            LoanPurposeOfUse purposeOfUse,
            BigDecimal amountIssued,
            BigDecimal amountPaid,
            BigDecimal balance,
            LocalDate plannedFinalDueDate,
            Integer amortizationFrequency
    ) {}

    public record RunningAccountLoan(
            BigDecimal creditLimit,
            BigDecimal balance,
            LocalDate balanceDate
    ) {}

    public record LeasingContract(
            LocalDate contractPeriodStartDate,
            BigDecimal transactionPrice
    ) {}

    public record DelayedAmount(
            BigDecimal delayedInstalment,
            LocalDate originalDueDate,
            Boolean isForeclosed
    ) {}

    public record IncomeData(
            Integer year,
            List<IncomeMonth> months
    ) {}

    public record IncomeMonth(
            Integer month,
            BigDecimal wagesGrossAmount,
            BigDecimal wagesNetAmount,
            BigDecimal benefitsGrossAmount,
            BigDecimal benefitsNetAmount
    ) {}

    public record DeceasedPerson(
            LocalDate dateOfDeath
    ) {}
}


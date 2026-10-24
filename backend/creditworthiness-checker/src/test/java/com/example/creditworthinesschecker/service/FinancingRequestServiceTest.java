package com.example.creditworthinesschecker.service;

import com.example.creditworthinesschecker.dto.CreditExtractRequest;
import com.example.creditworthinesschecker.dto.CreditExtractResponse;
import com.example.creditworthinesschecker.dto.FinancingRequestRequest;
import com.example.creditworthinesschecker.dto.FinancingRequestResponse;
import com.example.creditworthinesschecker.dto.PcrCreditRegisterExtractResponse;
import com.example.creditworthinesschecker.entity.FinancingRequest;
import com.example.creditworthinesschecker.enums.BanCheckStatus;
import com.example.creditworthinesschecker.enums.IdCodeType;
import com.example.creditworthinesschecker.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.enums.LoanType;
import com.example.creditworthinesschecker.exceptions.PcrRequestFailedException;
import com.example.creditworthinesschecker.repository.FinancingRequestRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FinancingRequestServiceTest {
    private final FinancingRequestRepository repository = mock(FinancingRequestRepository.class);
    private final CreditService creditService = mock(CreditService.class);
    private final FinancingRequestService service = new FinancingRequestService(repository, creditService);

    @Test
    void createMarksCheckedWhenVoluntaryBanPresent() {
        FinancingRequestRequest request = new FinancingRequestRequest(
                "TEST-002",
                LoanType.LumpSumLoan,
                LoanPurposeOfUse.OtherConsumerCredit
        );
        when(creditService.fetchCreditRegisterExtract(any(CreditExtractRequest.class)))
                .thenReturn(new CreditExtractResponse(extractWithBan(true), false));
        when(repository.save(any(FinancingRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        FinancingRequestResponse response = service.createFinancingRequest(request);

        assertEquals(BanCheckStatus.CHECKED, response.banCheckStatus());
        assertTrue(response.voluntaryBanInEffect());
        verify(repository).save(any(FinancingRequest.class));
    }

    @Test
    void createMarksFailedWhenPcrRejects() {
        FinancingRequestRequest request = new FinancingRequestRequest(
                "TEST-001",
                LoanType.LumpSumLoan,
                LoanPurposeOfUse.OtherConsumerCredit
        );
        when(creditService.fetchCreditRegisterExtract(any(CreditExtractRequest.class)))
                .thenThrow(new PcrRequestFailedException("PCR rejected", null));
        when(repository.save(any(FinancingRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        FinancingRequestResponse response = service.createFinancingRequest(request);

        assertEquals(BanCheckStatus.FAILED, response.banCheckStatus());
        assertNull(response.voluntaryBanInEffect());
    }

    @Test
    void findFlaggedDelegatesToRepository() {
        Instant after = Instant.parse("2026-01-01T00:00:00Z");
        FinancingRequest flagged = new FinancingRequest(
                "TEST-002",
                LoanType.LumpSumLoan,
                LoanPurposeOfUse.OtherConsumerCredit
        );
        flagged.setBanCheckStatus(BanCheckStatus.CHECKED);
        flagged.setVoluntaryBanInEffect(true);
        when(repository.findFlagged(BanCheckStatus.CHECKED, BanCheckStatus.FAILED, after))
                .thenReturn(List.of(flagged));

        List<FinancingRequestResponse> result = service.findFlagged(after);

        assertEquals(1, result.size());
        assertEquals("TEST-002", result.get(0).personIdCode());
        assertTrue(result.get(0).voluntaryBanInEffect());
        verify(repository).findFlagged(
                eq(BanCheckStatus.CHECKED),
                eq(BanCheckStatus.FAILED),
                eq(after));
    }

    @Test
    void findFlaggedDefaultsCreatedAfterToEpoch() {
        when(repository.findFlagged(BanCheckStatus.CHECKED, BanCheckStatus.FAILED, Instant.EPOCH))
                .thenReturn(List.of());

        assertTrue(service.findFlagged(null).isEmpty());
        verify(repository).findFlagged(BanCheckStatus.CHECKED, BanCheckStatus.FAILED, Instant.EPOCH);
    }

    private static PcrCreditRegisterExtractResponse extractWithBan(boolean banPresent) {
        return new PcrCreditRegisterExtractResponse(
                "OK",
                new PcrCreditRegisterExtractResponse.ExtractData(
                        UUID.randomUUID(),
                        Instant.now(),
                        new PcrCreditRegisterExtractResponse.PersonRequested(IdCodeType.PersonalIdentityCode, "TEST-002"),
                        new PcrCreditRegisterExtractResponse.Requester(IdCodeType.BusinessId, "1234567-8", "FI"),
                        banPresent
                                ? new PcrCreditRegisterExtractResponse.VoluntaryBanOnCredits(true, null)
                                : null,
                        null,
                        null,
                        null,
                        null
                )
        );
    }
}

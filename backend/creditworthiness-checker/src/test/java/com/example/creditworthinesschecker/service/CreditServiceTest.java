package com.example.creditworthinesschecker.service;

import com.example.creditworthinesschecker.dto.CreditExtractRequest;
import com.example.creditworthinesschecker.dto.CreditExtractResponse;
import com.example.creditworthinesschecker.dto.PcrCreditExtractRequest;
import com.example.creditworthinesschecker.dto.PcrCreditRegisterExtractResponse;
import com.example.creditworthinesschecker.dto.PcrRequesterProperties;
import com.example.creditworthinesschecker.enums.Country;
import com.example.creditworthinesschecker.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.enums.IdCodeType;
import com.example.creditworthinesschecker.exceptions.NotFoundException;
import com.example.creditworthinesschecker.exceptions.PcrRequestFailedException;
import com.example.creditworthinesschecker.exceptions.PcrUnavailableException;
import com.example.creditworthinesschecker.pcr.PcrClient;
import com.example.creditworthinesschecker.entity.CreditExtract;
import com.example.creditworthinesschecker.repository.CreditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreditServiceTest {

    private final PcrRequesterProperties properties = new PcrRequesterProperties(
            IdCodeType.PersonalIdentityCode,
            "1234567-8",
            Country.FI
    );
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final CreditRepository creditRepository = mock(CreditRepository.class);
    private final PcrClient pcrClient = mock(PcrClient.class);
    private final CreditService service = new CreditService(properties, objectMapper, creditRepository, pcrClient);

    @Test
    void fetchPersistsAndReturnsSuccessfulResponse() {
        CreditExtractRequest request = new CreditExtractRequest("TEST-001", CreditRegisterExtractPurpose.NewConsumerCredit);
        PcrCreditRegisterExtractResponse pcrResponse = new PcrCreditRegisterExtractResponse(
                "OK",
                new PcrCreditRegisterExtractResponse.ExtractData(
                        UUID.randomUUID(),
                        Instant.now(),
                        new PcrCreditRegisterExtractResponse.PersonRequested(IdCodeType.PersonalIdentityCode, "TEST-001"),
                        new PcrCreditRegisterExtractResponse.Requester(IdCodeType.PersonalIdentityCode, "1234567-8", "FI"),
                        new PcrCreditRegisterExtractResponse.VoluntaryBanOnCredits(false, null),
                        null,
                        null,
                        null,
                        null
                )
        );

        when(pcrClient.fetchExtract(any(PcrCreditExtractRequest.class))).thenReturn(pcrResponse);

        CreditExtractResponse response = service.fetchCreditRegisterExtract(request);

        assertNotNull(response);
        assertFalse(response.historical());
        assertEquals(pcrResponse, response.extract());
        verify(creditRepository).save(any(CreditExtract.class));
    }

    @Test
    void fetchUsesHistoricalFallbackWhenPcrUnavailable() {
        CreditExtractRequest request = new CreditExtractRequest("TEST-001", CreditRegisterExtractPurpose.NewConsumerCredit);
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("statusMessage", "OK");
        payload.putObject("creditRegisterExtract").put("extractReference", UUID.randomUUID().toString());

        CreditExtract historical = new CreditExtract(
                "TEST-001",
                UUID.randomUUID(),
                Instant.now().minusSeconds(120),
                true,
                payload
        );

        when(pcrClient.fetchExtract(any(PcrCreditExtractRequest.class))).thenThrow(new PcrUnavailableException("PCR is unavailable."));
        when(creditRepository.findFirstByPersonIdCodeOrderByFetchedAtDesc("TEST-001")).thenReturn(Optional.of(historical));

        CreditExtractResponse response = service.fetchCreditRegisterExtract(request);

        assertNotNull(response);
        assertTrue(response.historical());
        assertNotNull(response.extract());
        assertEquals(UUID.fromString(payload.get("creditRegisterExtract").get("extractReference").asText()),
                response.extract().creditRegisterExtract().extractReference());
    }

    @Test
    void fetchFailsForPcrRequestFailureWithoutFallback() {
        CreditExtractRequest request = new CreditExtractRequest("TEST-001", CreditRegisterExtractPurpose.NewConsumerCredit);

        when(pcrClient.fetchExtract(any(PcrCreditExtractRequest.class)))
                .thenThrow(new PcrRequestFailedException("PCR rejected request", new RuntimeException("bad")));

        PcrRequestFailedException ex = assertThrows(PcrRequestFailedException.class,
                () -> service.fetchCreditRegisterExtract(request));

        assertNotNull(ex.getMessage());
        verify(creditRepository, never()).save(any());
    }

    @Test
    void fetchFailsWhenPcrUnavailableAndNoHistoryExists() {
        CreditExtractRequest request = new CreditExtractRequest("TEST-001", CreditRegisterExtractPurpose.NewConsumerCredit);

        when(pcrClient.fetchExtract(any(PcrCreditExtractRequest.class)))
                .thenThrow(new PcrUnavailableException("PCR is unavailable."));
        when(creditRepository.findFirstByPersonIdCodeOrderByFetchedAtDesc("TEST-001")).thenReturn(Optional.empty());

        PcrRequestFailedException ex = assertThrows(PcrRequestFailedException.class,
                () -> service.fetchCreditRegisterExtract(request));

        assertEquals("PCR is unavailable and no historical extract exists.", ex.getMessage());
        verify(creditRepository).findFirstByPersonIdCodeOrderByFetchedAtDesc("TEST-001");
    }

    @Test
    void fetchWrapsRestClientFailures() {
        CreditExtractRequest request = new CreditExtractRequest("TEST-001", CreditRegisterExtractPurpose.NewConsumerCredit);

        when(pcrClient.fetchExtract(any(PcrCreditExtractRequest.class)))
                .thenThrow(new RestClientResponseException("bad", 500, "Internal Server Error", null, null, null));

        PcrRequestFailedException ex = assertThrows(PcrRequestFailedException.class,
                () -> service.fetchCreditRegisterExtract(request));

        assertTrue(ex.getMessage().contains("PCR returned HTTP 500"));
        verify(creditRepository, never()).save(any());
    }

    @Test
    void getExtractByIdReturnsHistoricalResponseWhenFound() {
        UUID extractId = UUID.randomUUID();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("statusMessage", "OK");
        payload.putObject("creditRegisterExtract").put("extractReference", extractId.toString());

        CreditExtract extract = new CreditExtract("TEST-001", extractId, Instant.now(), true, payload);
        when(creditRepository.findById(extractId)).thenReturn(Optional.of(extract));

        CreditExtractResponse response = service.getExtractById(extractId);

        assertNotNull(response);
        assertTrue(response.historical());
        assertNotNull(response.extract());
        assertEquals(extractId, response.extract().creditRegisterExtract().extractReference());
    }

    @Test
    void getExtractByIdThrowsNotFoundWhenMissing() {
        when(creditRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getExtractById(UUID.randomUUID()));

        assertEquals("Credit extract not found", ex.getMessage());
    }

    @Test
    void getExtractHistoryReturnsDescendingList() {
        CreditExtract first = new CreditExtract("TEST-001", UUID.randomUUID(), Instant.now(), false, objectMapper.createObjectNode());
        CreditExtract second = new CreditExtract("TEST-001", UUID.randomUUID(), Instant.now().minusSeconds(60), true, objectMapper.createObjectNode());

        when(creditRepository.findAllByPersonIdCodeOrderByFetchedAtDesc("TEST-001")).thenReturn(List.of(first, second));

        var history = service.getExtractHistory("TEST-001");

        assertEquals(2, history.size());
        assertEquals(first.getId(), history.get(0).id());
    }
}

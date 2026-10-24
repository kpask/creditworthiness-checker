package com.example.creditworthinesschecker.service;

import com.example.creditworthinesschecker.dto.*;
import com.example.creditworthinesschecker.exceptions.NotFoundException;
import com.example.creditworthinesschecker.exceptions.PcrRequestFailedException;
import com.example.creditworthinesschecker.exceptions.PcrUnavailableException;
import com.example.creditworthinesschecker.pcr.PcrClient;
import com.example.creditworthinesschecker.entity.CreditExtract;
import com.example.creditworthinesschecker.repository.CreditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreditService {
    private final PcrRequesterProperties pcrRequesterProperties;
    private final ObjectMapper objectMapper;
    private final CreditRepository creditRepository;
    private final PcrClient pcrClient;

    public CreditService(PcrRequesterProperties pcrRequesterProperties, ObjectMapper objectMapper, CreditRepository creditRepository, PcrClient pcrClient) {
        this.pcrRequesterProperties = pcrRequesterProperties;
        this.objectMapper = objectMapper;
        this.creditRepository = creditRepository;
        this.pcrClient = pcrClient;
    }

    public CreditExtractResponse fetchCreditRegisterExtract(CreditExtractRequest request) {
        try {
            PcrCreditExtractRequest pcrRequest = new PcrCreditExtractRequest(
                    pcrRequesterProperties.idCodeType(),
                    pcrRequesterProperties.idCode(),
                    pcrRequesterProperties.country(),
                    pcrRequesterProperties.idCodeType(),
                    request.personalIdCode(),
                    request.extractPurpose()
            );
            PcrCreditRegisterExtractResponse response = pcrClient.fetchExtract(pcrRequest);
            if(response == null || response.creditRegisterExtract() == null) {
                throw new PcrRequestFailedException("PCR returned a response without a credit register extract.", null);
            }

            boolean voluntaryBanInEffect = response.creditRegisterExtract().voluntaryBanOnCredits() != null;
            creditRepository.save(new CreditExtract(
                    request.personalIdCode(),
                    response.creditRegisterExtract().extractReference(),
                    Instant.now(),
                    voluntaryBanInEffect,
                    objectMapper.valueToTree(response)
            ));

            return toResponse(response);
        } catch (PcrUnavailableException ex) {
            CreditExtract extract = getLatestHistoricalExtract(request)
                    .orElseThrow(() -> new PcrRequestFailedException("PCR is unavailable and no historical extract exists.", ex));
            return toResponse(extract, true);
        } catch (PcrRequestFailedException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            throw new PcrRequestFailedException("PCR returned HTTP " + ex.getStatusCode(), ex);
        }
    }

    private Optional<CreditExtract> getLatestHistoricalExtract(CreditExtractRequest request) {
        return creditRepository.findFirstByPersonIdCodeOrderByFetchedAtDesc(request.personalIdCode());
    }

    public List<CreditExtractHistoryItem> getExtractHistory(String personIdCode) {
        return creditRepository.findAllByPersonIdCodeOrderByFetchedAtDesc(personIdCode)
                .stream()
                .map(extract -> new CreditExtractHistoryItem(
                        extract.getId(),
                        extract.getExtractReference(),
                        extract.getFetchedAt(),
                        extract.getVoluntaryBanInEffect()
                ))
                .toList();
    }

    public CreditExtractResponse toResponse(PcrCreditRegisterExtractResponse response) {
        return new CreditExtractResponse(response, false);
    }

    public CreditExtractResponse toResponse(CreditExtract creditExtract, boolean historical) {
        if (creditExtract == null || creditExtract.getResponseData() == null) {
            return new CreditExtractResponse(null, historical);
        }

        try {
            return new CreditExtractResponse(
                    objectMapper.treeToValue(creditExtract.getResponseData(), PcrCreditRegisterExtractResponse.class),
                    historical
            );
        } catch (Exception ex) {
            throw new PcrRequestFailedException("Historical PCR extract for ID " + creditExtract.getId() + " could not be read.", ex);
        }
    }

    public CreditExtractResponse getExtractById(UUID creditExtractId) {
        CreditExtract extract = creditRepository.findById(creditExtractId).orElse(null);
        if (extract == null) {
            throw new NotFoundException("Credit extract not found");
        }
        return toResponse(extract, true);
    }
}

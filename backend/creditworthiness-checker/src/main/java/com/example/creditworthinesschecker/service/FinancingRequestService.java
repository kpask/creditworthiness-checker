
package com.example.creditworthinesschecker.service;

import com.example.creditworthinesschecker.dto.*;
import com.example.creditworthinesschecker.entity.FinancingRequest;
import com.example.creditworthinesschecker.enums.BanCheckStatus;
import com.example.creditworthinesschecker.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.exceptions.NotFoundException;
import com.example.creditworthinesschecker.exceptions.PcrRequestFailedException;
import com.example.creditworthinesschecker.mapper.CreditRegisterExtractPurposeMapper;
import com.example.creditworthinesschecker.repository.FinancingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FinancingRequestService {
    private final FinancingRequestRepository financingRequestRepository;
    private final CreditService creditService;

    public FinancingRequestService(FinancingRequestRepository financingRequestRepository, CreditService creditService) {
        this.financingRequestRepository = financingRequestRepository;
        this.creditService = creditService;
    }

    public FinancingRequestResponse createFinancingRequest(FinancingRequestRequest request) {
        Boolean banInEffect = fetchVoluntaryBan(request);
        FinancingRequest financingRequest = new FinancingRequest(
                request.personIdCode(),
                request.loanType(),
                request.loanPurposeOfUse()
        );

        if (banInEffect == null) {
            financingRequest.setBanCheckStatus(BanCheckStatus.FAILED);
        } else {
            financingRequest.setBanCheckStatus(BanCheckStatus.CHECKED);
            financingRequest.setVoluntaryBanInEffect(banInEffect);
        }

        return toResponse(financingRequestRepository.save(financingRequest));
    }

    private Boolean fetchVoluntaryBan(FinancingRequestRequest request) {
        try {
            CreditRegisterExtractPurpose purpose = CreditRegisterExtractPurposeMapper.map(request.loanType(), request.loanPurposeOfUse());
            CreditExtractResponse response = creditService.fetchCreditRegisterExtract(new CreditExtractRequest(request.personIdCode(), purpose));

            if (response.historical()) {
                return null;
            }

            return response.extract().creditRegisterExtract().voluntaryBanOnCredits() != null;
        } catch (PcrRequestFailedException | NotFoundException ex) {
            System.err.println("PCR ban check failed: " + ex.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public FinancingRequestResponse getFinancingRequestById(UUID id) {
        FinancingRequest request = financingRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Financing request not found."));

        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<FinancingRequestResponse> getAllFinancingRequests() {
        return financingRequestRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FinancingRequestResponse> findFlagged(Instant createdAfter) {
        Instant since = createdAfter != null ? createdAfter : Instant.EPOCH;
        return financingRequestRepository
                .findFlagged(BanCheckStatus.CHECKED, BanCheckStatus.FAILED, since)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FinancingRequestResponse toResponse(FinancingRequest request) {
        return new FinancingRequestResponse(
                request.getId(),
                request.getPersonIdCode(),
                request.getLoanType(),
                request.getLoanPurposeOfUse(),
                request.getSubmittedAt(),
                request.getBanCheckStatus(),
                request.getVoluntaryBanInEffect()
        );
    }
}

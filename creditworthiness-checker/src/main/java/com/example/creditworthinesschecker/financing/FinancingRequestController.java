
package com.example.creditworthinesschecker.financing;

import com.example.creditworthinesschecker.financing.dto.FinancingRequestRequest;
import com.example.creditworthinesschecker.financing.dto.FinancingRequestResponse;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/financing-requests")
public class FinancingRequestController {

    private final FinancingRequestService financingRequestService;

    public FinancingRequestController(FinancingRequestService financingRequestService) {
        this.financingRequestService = financingRequestService;
    }

    @PostMapping
    public FinancingRequestResponse createFinancingRequest(@Valid @RequestBody FinancingRequestRequest request) {
        return financingRequestService.createFinancingRequest(request);
    }

    @GetMapping("/{id}")
    public FinancingRequestResponse getFinancingRequestById(@PathVariable UUID id) {
        return financingRequestService.getFinancingRequestById(id);
    }

    @GetMapping
    public List<FinancingRequestResponse> getAllFinancingRequests() {
        return financingRequestService.getAllFinancingRequests();
    }

    @GetMapping("/flagged")
    public List<FinancingRequestResponse> flagged(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter) {
        return financingRequestService.findFlagged(createdAfter);
    }
}

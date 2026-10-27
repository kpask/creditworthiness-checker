package com.example.creditworthinesschecker.credit;

import com.example.creditworthinesschecker.credit.dto.CreditExtractHistoryItem;
import com.example.creditworthinesschecker.credit.dto.CreditExtractRequest;
import com.example.creditworthinesschecker.credit.dto.CreditExtractResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
public class CreditController {
    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping("/fetch")
    public CreditExtractResponse fetchCreditRegisterExtract(@RequestBody @Valid CreditExtractRequest request) {
        return creditService.fetchCreditRegisterExtract(request);
    }

    @GetMapping("fetch/{personIdCode}/history")
    public List<CreditExtractHistoryItem> fetchCreditRegisterExtractHistory(@PathVariable @NotBlank String personIdCode) {
        return creditService.getExtractHistory(personIdCode);
    }

    @GetMapping("fetch/{creditExtractId}")
    public CreditExtractResponse fetchCreditRegisterExtractById(@PathVariable UUID creditExtractId) {
        return creditService.getExtractById(creditExtractId);
    }
}

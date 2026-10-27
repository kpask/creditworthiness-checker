package com.example.creditworthinesschecker.controller;

import com.example.creditworthinesschecker.credit.CreditController;
import com.example.creditworthinesschecker.credit.dto.CreditExtractHistoryItem;
import com.example.creditworthinesschecker.credit.dto.CreditExtractRequest;
import com.example.creditworthinesschecker.credit.dto.CreditExtractResponse;
import com.example.creditworthinesschecker.credit.dto.PcrCreditRegisterExtractResponse;
import com.example.creditworthinesschecker.credit.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.credit.enums.IdCodeType;
import com.example.creditworthinesschecker.credit.CreditService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditControllerTest {
    private final CreditService creditService = mock(CreditService.class);
    private final CreditController controller = new CreditController(creditService);

    @Test
    void fetchDelegatesToService() {
        CreditExtractRequest request = new CreditExtractRequest("TEST-001", CreditRegisterExtractPurpose.NewConsumerCredit);
        PcrCreditRegisterExtractResponse response = new PcrCreditRegisterExtractResponse(
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
        CreditExtractResponse expected = new CreditExtractResponse(response, false);

        when(creditService.fetchCreditRegisterExtract(request)).thenReturn(expected);

        CreditExtractResponse actual = controller.fetchCreditRegisterExtract(request);

        assertEquals(expected, actual);
        verify(creditService).fetchCreditRegisterExtract(request);
    }

    @Test
    void fetchHistoryDelegatesToService() {
        CreditExtractHistoryItem item = new CreditExtractHistoryItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                false
        );

        when(creditService.getExtractHistory("TEST-001")).thenReturn(List.of(item));

        List<CreditExtractHistoryItem> actual = controller.fetchCreditRegisterExtractHistory("TEST-001");

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(item, actual.get(0));
        verify(creditService).getExtractHistory("TEST-001");
    }
}

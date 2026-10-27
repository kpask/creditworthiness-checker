package com.example.creditworthinesschecker.pcr;

import com.example.creditworthinesschecker.credit.dto.PcrCreditExtractRequest;
import com.example.creditworthinesschecker.credit.dto.PcrErrorResponse;
import com.example.creditworthinesschecker.credit.enums.Country;
import com.example.creditworthinesschecker.credit.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.credit.enums.IdCodeType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class MockPcrControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockPcrController controller = new MockPcrController(objectMapper);

    @Test
    void fetchCreditExtractReturnsFixtureWhenFound() {
        PcrCreditExtractRequest request = new PcrCreditExtractRequest(
                IdCodeType.PersonalIdentityCode,
                "1234567-8",
                Country.FI,
                IdCodeType.PersonalIdentityCode,
                "TEST-001",
                CreditRegisterExtractPurpose.NewConsumerCredit
        );

        ResponseEntity<?> response = controller.fetchCreditExtract(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof JsonNode);
    }

    @Test
    void fetchCreditExtractReturnsSection35ErrorWhenNotFound() {
        PcrCreditExtractRequest request = new PcrCreditExtractRequest(
                IdCodeType.PersonalIdentityCode,
                "1234567-8",
                Country.FI,
                IdCodeType.PersonalIdentityCode,
                "UNKNOWN-999",
                CreditRegisterExtractPurpose.NewConsumerCredit
        );

        ResponseEntity<?> response = controller.fetchCreditExtract(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof PcrErrorResponse);

        PcrErrorResponse errorResponse = (PcrErrorResponse) response.getBody();
        assertEquals("Bad request", errorResponse.statusMessage());
        assertNotNull(errorResponse.correlationId());
        assertNotNull(errorResponse.errorResponses());
        assertEquals(1, errorResponse.errorResponses().size());

        PcrErrorResponse.ErrorItem item = errorResponse.errorResponses().get(0);
        assertEquals("requestedIdCode", item.fieldName());
        assertEquals("PERSON_NOT_FOUND", item.errorCode());
        assertEquals("Requested person was not found in the credit register.", item.errorDescription());
    }

    @Test
    void fetchCreditExtractReturnsServiceUnavailableWhenNotAvailable() {
        controller.setAvailable(false);
        try {
            PcrCreditExtractRequest request = new PcrCreditExtractRequest(
                    IdCodeType.PersonalIdentityCode,
                    "1234567-8",
                    Country.FI,
                    IdCodeType.PersonalIdentityCode,
                    "TEST-001",
                    CreditRegisterExtractPurpose.NewConsumerCredit
            );

            ResponseEntity<?> response = controller.fetchCreditExtract(request);

            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        } finally {
            controller.setAvailable(true);
        }
    }
}

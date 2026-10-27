package com.example.creditworthinesschecker.pcr;

import com.example.creditworthinesschecker.credit.dto.PcrCreditExtractRequest;
import com.example.creditworthinesschecker.credit.enums.Country;
import com.example.creditworthinesschecker.credit.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.credit.enums.IdCodeType;
import com.example.creditworthinesschecker.common.exceptions.NotFoundException;
import com.example.creditworthinesschecker.common.exceptions.PcrRequestFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RestPcrClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fetchExtractThrowsNotFoundExceptionWhenPersonNotFound() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8080");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestPcrClient client = new RestPcrClient(builder.build(), objectMapper);

        String errorJson = """
                {
                  "statusMessage": "Bad request",
                  "correlationId": "48bb19eb-b5c6-4d7a-b5e1-2e6b20677598",
                  "errorResponses": [
                    {
                      "fieldName": "requestedIdCode",
                      "errorCode": "PERSON_NOT_FOUND",
                      "errorDescription": "Requested person was not found in the credit register."
                    }
                  ]
                }
                """;

        server.expect(requestTo("http://localhost:8080/mock-pcr/credit-register-extract"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson));

        PcrCreditExtractRequest request = new PcrCreditExtractRequest(
                IdCodeType.PersonalIdentityCode,
                "1234567-8",
                Country.FI,
                IdCodeType.PersonalIdentityCode,
                "UNKNOWN-999",
                CreditRegisterExtractPurpose.NewConsumerCredit
        );

        NotFoundException ex = assertThrows(NotFoundException.class, () -> client.fetchExtract(request));
        assertEquals("Requested person was not found in the credit register.", ex.getMessage());
        server.verify();
    }

    @Test
    void fetchExtractThrowsPcrRequestFailedExceptionOnOtherBadRequest() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8080");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestPcrClient client = new RestPcrClient(builder.build(), objectMapper);

        String errorJson = """
                {
                  "statusMessage": "Bad request",
                  "correlationId": "48bb19eb-b5c6-4d7a-b5e1-2e6b20677598",
                  "errorResponses": [
                    {
                      "fieldName": "extractPurpose",
                      "errorCode": "INVALID_PURPOSE",
                      "errorDescription": "Extract purpose not permitted for this loan type."
                    }
                  ]
                }
                """;

        server.expect(requestTo("http://localhost:8080/mock-pcr/credit-register-extract"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson));

        PcrCreditExtractRequest request = new PcrCreditExtractRequest(
                IdCodeType.PersonalIdentityCode,
                "1234567-8",
                Country.FI,
                IdCodeType.PersonalIdentityCode,
                "TEST-001",
                CreditRegisterExtractPurpose.NewConsumerCredit
        );

        PcrRequestFailedException ex = assertThrows(PcrRequestFailedException.class, () -> client.fetchExtract(request));
        assertTrue(ex.getMessage().contains("Extract purpose not permitted for this loan type."));
        server.verify();
    }
}

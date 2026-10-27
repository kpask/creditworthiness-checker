package com.example.creditworthinesschecker.pcr;

import com.example.creditworthinesschecker.credit.dto.PcrCreditExtractRequest;
import com.example.creditworthinesschecker.credit.dto.PcrCreditRegisterExtractResponse;
import com.example.creditworthinesschecker.credit.dto.PcrErrorResponse;
import com.example.creditworthinesschecker.common.exceptions.NotFoundException;
import com.example.creditworthinesschecker.common.exceptions.PcrRequestFailedException;
import com.example.creditworthinesschecker.common.exceptions.PcrUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.stream.Collectors;

@Component
public class RestPcrClient implements PcrClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public RestPcrClient(
            @Value("${pcr.base-url:http://localhost:8080}") String baseUrl,
            @Value("${pcr.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${pcr.read-timeout-ms:5000}") long readTimeoutMs,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofMillis(connectTimeoutMs).toMillis());
                    setReadTimeout((int) Duration.ofMillis(readTimeoutMs).toMillis());
                }})
                .build();
    }

    RestPcrClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public PcrCreditRegisterExtractResponse fetchExtract(PcrCreditExtractRequest request) {
        try {
            PcrCreditRegisterExtractResponse response = restClient.post()
                    .uri("/mock-pcr/credit-register-extract")
                    .body(request)
                    .retrieve()
                    .body(PcrCreditRegisterExtractResponse.class);

            if (response == null) {
                throw new PcrRequestFailedException("PCR returned an empty response.", null);
            }

            return response;
        } catch (HttpClientErrorException ex) {
            PcrErrorResponse errorResponse = parsePcrError(ex.getResponseBodyAsString());

            if (errorResponse != null && errorResponse.errorResponses() != null && !errorResponse.errorResponses().isEmpty()) {
                boolean isNotFound = errorResponse.errorResponses().stream()
                        .anyMatch(err -> "PERSON_NOT_FOUND".equalsIgnoreCase(err.errorCode()));

                String descriptions = errorResponse.errorResponses().stream()
                        .map(PcrErrorResponse.ErrorItem::errorDescription)
                        .filter(desc -> desc != null && !desc.isBlank())
                        .collect(Collectors.joining("; "));

                if (descriptions.isBlank()) {
                    descriptions = errorResponse.statusMessage() != null ? errorResponse.statusMessage() : "Bad request";
                }

                if (isNotFound) {
                    throw new NotFoundException(descriptions);
                }

                throw new PcrRequestFailedException("PCR rejected the request: " + descriptions, ex);
            }

            throw new PcrRequestFailedException("PCR rejected the request with HTTP " + ex.getStatusCode(), ex);
        } catch (HttpServerErrorException ex) {
            if (ex.getStatusCode().is5xxServerError()) {
                throw new PcrUnavailableException("PCR is unavailable.", ex);
            }
            throw new PcrRequestFailedException("PCR returned HTTP " + ex.getStatusCode(), ex);
        } catch (ResourceAccessException ex) {
            throw new PcrUnavailableException("PCR connection timed out or was unreachable.", ex);
        } catch (RestClientResponseException ex) {
            throw new PcrRequestFailedException("PCR returned HTTP " + ex.getStatusCode(), ex);
        }
    }

    private PcrErrorResponse parsePcrError(String body) {
        if (body == null || body.isBlank()){
            return null;
        }

        try {
            return objectMapper.readValue(body, PcrErrorResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
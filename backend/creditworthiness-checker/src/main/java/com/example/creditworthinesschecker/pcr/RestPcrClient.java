
package com.example.creditworthinesschecker.pcr;

import com.example.creditworthinesschecker.dto.PcrCreditExtractRequest;
import com.example.creditworthinesschecker.dto.PcrCreditRegisterExtractResponse;
import com.example.creditworthinesschecker.exceptions.PcrRequestFailedException;
import com.example.creditworthinesschecker.exceptions.PcrUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Component
public class RestPcrClient implements PcrClient {
    private final RestClient restClient;

    public RestPcrClient(
            @Value("${pcr.base-url:http://localhost:8080}") String baseUrl,
            @Value("${pcr.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${pcr.read-timeout-ms:5000}") long readTimeoutMs
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofMillis(connectTimeoutMs).toMillis());
                    setReadTimeout((int) Duration.ofMillis(readTimeoutMs).toMillis());
                }})
                .build();
    }

    public PcrCreditRegisterExtractResponse fetchExtract(PcrCreditExtractRequest request) {
        System.out.println("PCR URL: http://localhost:8080/mock-pcr/credit-register-extract");
        try {
            return restClient.post()
                    .uri("/mock-pcr/credit-register-extract")
                    .body(request)
                    .retrieve()
                    .body(PcrCreditRegisterExtractResponse.class);
        } catch (HttpClientErrorException ex) {
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
}

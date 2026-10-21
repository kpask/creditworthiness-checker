
package com.example.creditworthinesschecker.pcr;

import com.example.creditworthinesschecker.dto.PcrCreditExtractRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/mock-pcr")
public class MockPcrController {
    private final ObjectMapper objectMapper;
    @Value("${mock-pcr.available:true}")
    private boolean available;
    private final Map<String, String> fixtures = Map.of(
            "TEST-001", "mock-pcr/consumer-001.json",
            "TEST-002", "mock-pcr/consumer-002.json",
            "TEST-003", "mock-pcr/consumer-003.json",
            "TEST-004", "mock-pcr/consumer-004.json"
    );

    public MockPcrController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/credit-register-extract")
    public ResponseEntity<JsonNode> fetchCreditExtract(@RequestBody @Valid PcrCreditExtractRequest request) {
        if (!available) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .build();
        }

        String fixturePath = fixtures.get(request.requestedIdCode());
        if (fixturePath == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            JsonNode response = objectMapper.readTree(new ClassPathResource(fixturePath).getInputStream());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load PCR mock fixture: " + fixturePath, e);
        }
    }
}

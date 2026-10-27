package com.example.creditworthinesschecker.exceptions;

import com.example.creditworthinesschecker.common.exceptions.GlobalExceptionHandler;
import com.example.creditworthinesschecker.common.exceptions.NotFoundException;
import com.example.creditworthinesschecker.common.exceptions.PcrRequestFailedException;
import com.example.creditworthinesschecker.common.exceptions.PcrUnavailableException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void validationExceptionMapsToBadRequest() {
        Map<String, Object> response = handler.handleConstraintViolation(new ConstraintViolationException("bad", java.util.Set.of())).getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.get("status"));
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.get("error"));
    }

    @Test
    void notFoundExceptionMapsTo404() {
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(new NotFoundException("missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().get("message"));
    }

    @Test
    void pcrUnavailableMapsTo503() {
        ResponseEntity<Map<String, Object>> response = handler.handlePcrUnavailable(new PcrUnavailableException("PCR is down"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("PCR is down", response.getBody().get("message"));
    }

    @Test
    void pcrRequestFailedMapsTo502() {
        ResponseEntity<Map<String, Object>> response = handler.handlePcrRequestFailed(new PcrRequestFailedException("PCR failed", new RuntimeException("boom")));

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("PCR failed", response.getBody().get("message"));
    }

    @Test
    void unexpectedExceptionMapsTo500() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnexpected(new RuntimeException("oops"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected server error.", response.getBody().get("message"));
    }
}

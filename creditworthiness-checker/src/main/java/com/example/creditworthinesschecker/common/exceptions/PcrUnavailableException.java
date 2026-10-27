package com.example.creditworthinesschecker.common.exceptions;

public class PcrUnavailableException extends RuntimeException {
    public PcrUnavailableException(String message) {
        super(message);
    }

    public PcrUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
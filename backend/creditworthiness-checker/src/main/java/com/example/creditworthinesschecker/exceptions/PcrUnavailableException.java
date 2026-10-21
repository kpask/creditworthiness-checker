package com.example.creditworthinesschecker.exceptions;

public class PcrUnavailableException extends RuntimeException {
    public PcrUnavailableException(String message) {
        super(message);
    }

    public PcrUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
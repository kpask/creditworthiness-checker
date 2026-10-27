package com.example.creditworthinesschecker.common.exceptions;

public class PcrRequestFailedException extends RuntimeException {
    public PcrRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
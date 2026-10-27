package com.fplassistant.exception;

public class FplApiException extends RuntimeException {
    public FplApiException(String message) {
        super(message);
    }

    public FplApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

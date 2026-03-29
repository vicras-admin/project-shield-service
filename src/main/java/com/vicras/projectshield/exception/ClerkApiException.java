package com.vicras.projectshield.exception;

public class ClerkApiException extends RuntimeException {

    public ClerkApiException(String message) {
        super(message);
    }

    public ClerkApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

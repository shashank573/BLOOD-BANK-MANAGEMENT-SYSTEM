package com.bloodbank.core;

public class InvalidEligibilityException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidEligibilityException(String errorMessage) {
        super(errorMessage);
    }

    public InvalidEligibilityException(String errorMessage, Throwable rootCause) {
        super(errorMessage, rootCause);
    }
}
package com.venue.mgmt.exception;

public class RuleAlreadyExistsException extends RuntimeException {
    public RuleAlreadyExistsException(String message) {
        super(message);
    }

    public RuleAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

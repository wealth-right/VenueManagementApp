package com.venue.mgmt.exception;

public class LeadNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LeadNotFoundException(String message) {
        super(message);
    }

    public LeadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

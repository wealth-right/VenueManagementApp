package com.venue.mgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LeadNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LeadNotFoundException(String message) {
        super(message);
    }

    public LeadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

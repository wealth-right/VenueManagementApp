package com.venue.mgmt.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends HttpStatusException {
    public AlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

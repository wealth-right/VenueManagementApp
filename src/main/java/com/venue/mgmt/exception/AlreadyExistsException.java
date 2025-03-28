package com.venue.mgmt.exception;


public class AlreadyExistsException extends HttpStatusException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}

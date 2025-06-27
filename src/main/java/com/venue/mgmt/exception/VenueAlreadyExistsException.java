package com.venue.mgmt.exception;

public class VenueAlreadyExistsException extends RuntimeException {
    public VenueAlreadyExistsException(String s) {
        super(s);
    }
}

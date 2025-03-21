package com.venue.mgmt.exception;

public class VenueNotSavedException extends RuntimeException {
    public VenueNotSavedException(String message) {
        super(message);
    }
}

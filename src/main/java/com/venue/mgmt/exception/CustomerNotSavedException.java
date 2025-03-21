package com.venue.mgmt.exception;

public class CustomerNotSavedException extends RuntimeException{
    public CustomerNotSavedException(String message) {
        super(message);
    }
}

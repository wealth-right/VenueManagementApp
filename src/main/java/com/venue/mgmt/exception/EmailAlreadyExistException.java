package com.venue.mgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistException extends RuntimeException {
    public EmailAlreadyExistException(String s) {
        super(s);
    }
}

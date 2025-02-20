package com.venue.mgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserIdNotExist extends RuntimeException {
    public UserIdNotExist(String message) {
        super(message);
    }
}

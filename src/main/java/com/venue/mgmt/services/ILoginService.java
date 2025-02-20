package com.venue.mgmt.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.venue.mgmt.request.LoginRequest;
import com.venue.mgmt.response.ValidateUserResponse;
import org.springframework.http.ResponseEntity;

public interface ILoginService {
    ValidateUserResponse login(LoginRequest loginRequest) throws JsonProcessingException;
}

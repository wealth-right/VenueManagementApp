package com.venue.mgmt.controller;

import com.venue.mgmt.request.LoginRequest;
import com.venue.mgmt.request.VerifyUserOtpRequest;
import com.venue.mgmt.response.ValidateUserResponse;
import com.venue.mgmt.services.ILoginService;
import com.venue.mgmt.services.INotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/venue-app/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Login Controller", description = "API for Customer to login")
@CrossOrigin(origins = "*")
public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    
    @Autowired
    private ILoginService loginService;
    
    @Autowired
    private INotificationService notificationService;

    @PostMapping(value = "/sendOtp",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidateUserResponse login(@RequestBody @Valid LoginRequest loginRequest) throws Exception {
        logger.info("LoginController - Inside login method");
        return loginService.login(loginRequest);
    }

    @PostMapping(value = "/verify-otp",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> verifyOtp(@RequestBody @Valid VerifyUserOtpRequest verifyRequest) {
        logger.info("LoginController - Inside verifyOtp method");
        return  notificationService.validateOtp(verifyRequest);
    }
}
package com.venue.mgmt.controller;

import com.venue.mgmt.entities.OtpDetails;
import com.venue.mgmt.request.ValidateOtpRequest;
import com.venue.mgmt.services.OTPService;
import com.venue.mgmt.util.JWTValidator;
import com.venue.mgmt.util.JwtUtil;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/venue-app/v1")
public class OtpDetailController {
    private static final Logger logger = LogManager.getLogger(OtpDetailController.class);

    @Autowired
    private OTPService otpService;

    @PostMapping("/sendOtp")
    public ResponseEntity<?> sendOtp(@RequestHeader(name = "Authorization", required = true) String authHeader,
                                     @RequestBody @Valid ValidateOtpRequest validateOtpRequest) throws Exception {
        logger.info("VenueManagementApp - Inside send otp method with lead Id : {}", validateOtpRequest.getLeadId());
        if(JWTValidator.validateToken(authHeader)){
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }
        }
        String userId = JwtUtil.extractUserIdFromToken(authHeader);
        OtpDetails otpDetails = otpService.generateAndSendOTP(validateOtpRequest,userId);
        return ResponseEntity.ok(otpDetails);
    }

    @PostMapping("/validateOtp")
    public ResponseEntity<?> validateOtp(@RequestHeader(name="Authorization", required = true) String authHeader,
            @RequestBody @Valid ValidateOtpRequest validateOtpRequest) throws Exception {
        logger.info("VenueManagementApp - Inside validate otp method");
        if(JWTValidator.validateToken(authHeader)){
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }
        }
        return ResponseEntity.ok(otpService.validateOtp(validateOtpRequest));
    }



}

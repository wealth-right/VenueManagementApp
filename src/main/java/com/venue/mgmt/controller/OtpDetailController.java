package com.venue.mgmt.controller;

import com.venue.mgmt.constant.GeneralMsgConstants;
import com.venue.mgmt.entities.OtpDetails;
import com.venue.mgmt.request.ValidateOtpRequest;
import com.venue.mgmt.response.VerifyUserOtpResponse;
import com.venue.mgmt.services.OTPService;
import com.venue.mgmt.util.JWTValidator;
import com.venue.mgmt.util.JwtUtil;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/venue-app/v1/leads")
public class OtpDetailController {
    private static final Logger logger = LogManager.getLogger(OtpDetailController.class);

    private final OTPService otpService;


    public OtpDetailController(OTPService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/sendOtp")
    public ResponseEntity<VerifyUserOtpResponse> sendOtp(@RequestHeader(name = "Authorization", required = true) String authHeader,
                                     @RequestBody @Valid ValidateOtpRequest validateOtpRequest) throws Exception {
        logger.info("VenueManagementApp - Inside send otp method with lead Id : {}", validateOtpRequest.getLeadId());
        if(JWTValidator.validateToken(authHeader)){
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }
        }
        String userId = JwtUtil.extractUserIdFromToken(authHeader);
        Boolean otpSentFlag = otpService.generateAndSendOTP(validateOtpRequest, userId);
        VerifyUserOtpResponse verifyUserOtpResponse = new VerifyUserOtpResponse();
        verifyUserOtpResponse.setStatusCode(200);
        verifyUserOtpResponse.setStatusMsg(GeneralMsgConstants.OTP_SENT_SUCCESS);
        verifyUserOtpResponse.setErrorMsg(null);
        verifyUserOtpResponse.setResponse(otpSentFlag);
        return ResponseEntity.ok(verifyUserOtpResponse);
    }

    @PostMapping("/validateOtp")
    public ResponseEntity<VerifyUserOtpResponse> validateOtp(@RequestHeader(name="Authorization", required = true) String authHeader,
            @RequestBody @Valid ValidateOtpRequest validateOtpRequest) throws Exception {
        logger.info("VenueManagementApp - Inside validate otp method");
        if(JWTValidator.validateToken(authHeader)){
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }
        }
        boolean otpVerifiedSuccessfully = otpService.validateOtp(validateOtpRequest);
        VerifyUserOtpResponse verifyUserOtpResponse = new VerifyUserOtpResponse();
        verifyUserOtpResponse.setStatusCode(200);
        verifyUserOtpResponse.setStatusMsg(GeneralMsgConstants.OTP_VERIFIED_SUCCESS);
        verifyUserOtpResponse.setErrorMsg(null);
        verifyUserOtpResponse.setResponse(otpVerifiedSuccessfully);
        return ResponseEntity.ok(verifyUserOtpResponse);
    }



}

package com.venue.mgmt.controller;

import com.venue.mgmt.constant.GeneralMsgConstants;
import com.venue.mgmt.request.ValidateOtpRequest;
import com.venue.mgmt.response.VerifyUserOtpResponse;
import com.venue.mgmt.services.OTPService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/venue-app/v1/leads")
public class OtpDetailController {
    private static final Logger logger = LogManager.getLogger(OtpDetailController.class);

    private final OTPService otpService;
    private final HttpServletRequest request;


    public OtpDetailController(OTPService otpService, HttpServletRequest request) {
        this.otpService = otpService;
        this.request = request;
    }

    @PostMapping("/sendOtp")
    public ResponseEntity<VerifyUserOtpResponse> sendOtp(
            @RequestBody @Valid ValidateOtpRequest validateOtpRequest) {
        logger.info("VenueManagementApp - Inside send otp method with lead Id : {}", validateOtpRequest.getLeadId());
        try {
            String userId = (String) request.getAttribute(GeneralMsgConstants.USER_ID);
            String messageSent = otpService.generateAndSendOTP(validateOtpRequest, userId);
            VerifyUserOtpResponse verifyUserOtpResponse;
            verifyUserOtpResponse = new VerifyUserOtpResponse();
            verifyUserOtpResponse.setStatusCode(200);
            verifyUserOtpResponse.setStatusMsg(GeneralMsgConstants.OTP_SENT_SUCCESS);
            verifyUserOtpResponse.setErrorMsg(null);
            verifyUserOtpResponse.setResponse(!messageSent.isEmpty());
            return ResponseEntity.ok(verifyUserOtpResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/validateOtp")
    public ResponseEntity<VerifyUserOtpResponse> validateOtp(
            @RequestBody @Valid ValidateOtpRequest validateOtpRequest) {
        logger.info("VenueManagementApp - Inside validate otp method");
        VerifyUserOtpResponse verifyUserOtpResponse = new VerifyUserOtpResponse();
        try {
            boolean otpVerifiedSuccessfully = otpService.validateOtp(validateOtpRequest);
            verifyUserOtpResponse.setStatusCode(200);
            verifyUserOtpResponse.setStatusMsg(GeneralMsgConstants.OTP_VERIFIED_SUCCESS);
            verifyUserOtpResponse.setErrorMsg(null);
            verifyUserOtpResponse.setResponse(otpVerifiedSuccessfully);
            return ResponseEntity.ok(verifyUserOtpResponse);
        } catch (Exception e) {
            verifyUserOtpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            verifyUserOtpResponse.setStatusMsg(GeneralMsgConstants.OTP_VERIFIED_FAILED);
            verifyUserOtpResponse.setErrorMsg(e.getMessage());
            verifyUserOtpResponse.setResponse(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(verifyUserOtpResponse);
        }
    }


}

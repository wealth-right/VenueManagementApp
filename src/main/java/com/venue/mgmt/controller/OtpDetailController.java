package com.venue.mgmt.controller;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.OtpDetails;
import com.venue.mgmt.services.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verify")
public class OtpDetailController {

    @Autowired
    private OTPService otpService;

    @PostMapping("/sendOtp/{leadId}")
    public OtpDetails sendOtp(@PathVariable Long leadId){
        return otpService.generateAndSendOTP(leadId);
    }



}

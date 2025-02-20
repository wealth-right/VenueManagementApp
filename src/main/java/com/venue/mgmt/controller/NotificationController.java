package com.venue.mgmt.controller;

import com.venue.mgmt.request.LoginRequest;
import com.venue.mgmt.response.ValidateUserResponse;
import com.venue.mgmt.services.INotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notification")
@Tag(name = "Notification Controller", description = "API to send notification to customer")
public class NotificationController {
    private static final Logger logger = LogManager.getLogger(NotificationController.class);
    @Autowired
    private INotificationService notificationService;


//    @PostMapping("/otp/resend")
//    public ValidateUserResponse resendOtpOnNumber(@RequestParam LoginRequest loginReq) {
//        logger.info("NotificationController - Inside resendOtpOnNumber method");
//        return notificationService.sendOtpOnNumber(loginReq);
//    }
}
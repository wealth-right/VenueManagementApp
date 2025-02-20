package com.venue.mgmt.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.request.LoginRequest;
import com.venue.mgmt.response.ValidateUserResponse;
import com.venue.mgmt.services.ILoginService;
import com.venue.mgmt.services.INotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements ILoginService {
    private static final Logger logger = LogManager.getLogger(LoginServiceImpl.class);
    
    @Autowired
    private LeadRegRepository leadRegRepository;
    
    @Autowired
    private INotificationService notificationService;

    @Override
    public ValidateUserResponse login(LoginRequest loginRequest) throws JsonProcessingException {
        logger.info("LoginServiceImpl - Inside login method");
        return notificationService.sendOtpOnNumber(loginRequest);
    }
}
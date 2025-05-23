package com.venue.mgmt.services;

import com.venue.mgmt.constant.ErrorMsgConstants;
import com.venue.mgmt.entities.OTP;
import com.venue.mgmt.entities.OtpDetails;
import com.venue.mgmt.exception.AlreadyExistsException;
import com.venue.mgmt.exception.InvalidOtpException;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.OtpDetailsRepository;
import com.venue.mgmt.request.ValidateOtpRequest;
import com.venue.mgmt.util.CommonUtils;
import com.venue.mgmt.util.OtpDetailsUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.venue.mgmt.constant.ErrorMsgConstants.LIMIT_EXCEEDED;
import static com.venue.mgmt.constant.ErrorMsgConstants.MINUTES;
import static com.venue.mgmt.constant.GeneralMsgConstants.MAIL_BODY;

@Service
public class OTPService extends OtpDetailsUtils {

    private final OtpDetailsRepository otpDetailsRepository;

    private final LeadRegRepository leadRegRepository;

    private final OTP otpPath;

    public OTPService(OtpDetailsRepository otpDetailsRepository, LeadRegRepository leadRegRepository, OTP otpPath) {
        this.otpDetailsRepository = otpDetailsRepository;
        this.leadRegRepository = leadRegRepository;
        this.otpPath = otpPath;
    }

    private static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

    private static final Logger logger = LogManager.getLogger(OTPService.class);

    public String generateAndSendOTP(ValidateOtpRequest validateOtpRequest, String userId) {
        Optional<OtpDetails> optionalOtpDetails;
        LocalDateTime lastCreationDate;
        logger.info("OTPService - Inside generateAndSendOTP method");
        List<OtpDetails> otpDetailsList = otpDetailsRepository.findByLeadIdAndMobileNo(validateOtpRequest.getLeadId(), validateOtpRequest.getMobileNumber());
        if (!otpDetailsList.isEmpty() && otpDetailsList.size() >= otpPath.getNoOfAttempt()) {
            optionalOtpDetails = otpDetailsList.stream().findFirst();
            lastCreationDate = CommonUtils.getLocalDateTime(optionalOtpDetails.get().getCreatedAt());
            long timeSinceLastAttempt = ChronoUnit.MILLIS.between(lastCreationDate, LocalDateTime.now());
            if (timeSinceLastAttempt < otpPath.getBlockTime()) {
                long remainingTimeMinutes = (otpPath.getBlockTime() - timeSinceLastAttempt) / 60000;
                throw new AlreadyExistsException(LIMIT_EXCEEDED + remainingTimeMinutes + ErrorMsgConstants.MINUTES);
            }
        }
        OtpDetails otpDetails = new OtpDetails();
        long otp = generateOTPSMS();
        logger.info("OTP generated: {}", otp);
        String message = MAIL_BODY.replaceFirst("\\{#var#}",
                String.valueOf(otp)).replaceFirst("\\{#var#}",
                String.valueOf(otpPath.getOtpExpiry() / 60000));
        message = URLDecoder.decode(message, StandardCharsets.UTF_8);
        String messageSent=null;
        try {
            messageSent = sendSMS(validateOtpRequest.getMobileNumber(), message);
            otpDetails.setMobileNo(validateOtpRequest.getMobileNumber());
            otpDetails.setLeadId(validateOtpRequest.getLeadId());
            otpDetails.setOtp((String.valueOf(otp)));
            otpDetails.setSmsResponse(message);
            otpDetails.setCreatedBy(userId);
            otpDetailsRepository.save(otpDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageSent;
    }

    public boolean validateOtp(ValidateOtpRequest validateOtpRequest) {
        // Fetch the latest OTP record based on leadId and mobileNumber
        List<OtpDetails> otpDetailsList = otpDetailsRepository.findByLeadIdAndMobileNo(
                validateOtpRequest.getLeadId(), validateOtpRequest.getMobileNumber());
        if (otpDetailsList != null && !otpDetailsList.isEmpty()) {
            // Sort the list by creation date in descending order and get the first record
            OtpDetails latestOtpDetails = otpDetailsList.stream()
                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                    .findFirst()
                    .orElse(null);
            // Validate the OTP
            long currentTime = Instant.now().toEpochMilli();
            long otpCreationTime = latestOtpDetails.getCreatedAt().toInstant().toEpochMilli();
            long otpExpiryTime = otpCreationTime + OTP_VALID_DURATION;
            if (latestOtpDetails.getAttempts() >= 3) {
                LocalDateTime creationDateTime = latestOtpDetails.getCreatedAt().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                long timeSinceLastAttempt = ChronoUnit.MILLIS.between(creationDateTime, LocalDateTime.now());
                if (timeSinceLastAttempt < otpPath.getBlockTime()) {
                    long remainingTimeMinutes = (otpPath.getBlockTime() - timeSinceLastAttempt) / 60000;
                    throw new AlreadyExistsException(LIMIT_EXCEEDED + remainingTimeMinutes + MINUTES);
                } else {
                    latestOtpDetails.setAttempts(0); // Reset attempts after block time
                }
            }
            // Check if the OTP is still valid
            if (latestOtpDetails.getOtp().equals(validateOtpRequest.getOtp()) && currentTime <= otpExpiryTime) {
                leadRegRepository.findById(validateOtpRequest.getLeadId()).ifPresent(lead -> {
                    lead.setMobileVerified(true);
                    leadRegRepository.save(lead);
                });
                latestOtpDetails.setAttempts(0); // Reset attempts on successful validation
                latestOtpDetails.setVerified(true);
                otpDetailsRepository.save(latestOtpDetails);
                return true;
            } else {
                latestOtpDetails.setAttempts(latestOtpDetails.getAttempts() + 1); // Increment attempts on failure
                otpDetailsRepository.save(latestOtpDetails);
                int attemptsLeft = 3 - latestOtpDetails.getAttempts();
                throw new InvalidOtpException("Please enter a valid OTP. You have " + attemptsLeft + " attempts left.");
            }
        }
        return false;
    }

}

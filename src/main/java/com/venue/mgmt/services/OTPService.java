package com.venue.mgmt.services;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.OTP;
import com.venue.mgmt.entities.OtpDetails;
import com.venue.mgmt.exception.AlreadyExistsException;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.OtpDetailsRepository;
import com.venue.mgmt.util.CommonUtils;
import com.venue.mgmt.util.OtpDetailsUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.venue.mgmt.constant.GeneralMsgConstants.*;

@Service
public class OTPService extends OtpDetailsUtils {
    private final Map<String, OTPData> otpStore = new HashMap<>();

    @Autowired
    private OtpDetailsRepository otpDetailsRepository;

    @Autowired
    private LeadRegRepository leadRegRepository;

    @Autowired
    private OTP otpPath;
    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

    @Autowired
    private RestTemplate restTemplate;
    private static final Logger logger = LogManager.getLogger(OTPService.class);
    public OtpDetails generateAndSendOTP(Long leadId){
        Optional<OtpDetails> optionalOtpDetails;
        LocalDateTime lastCreationDate;
        logger.info("OTPService - Inside generateAndSendOTP method");
        Optional<LeadRegistration> leadRecord = leadRegRepository.findByLeadId(leadId);
        String mobileNumber = null;
        if(leadRecord.isPresent()) {
            mobileNumber = leadRecord.get().getMobileNumber();
            List<OtpDetails> otpDetailsList = otpDetailsRepository.findByMobileNo(mobileNumber);
            if (!otpDetailsList.isEmpty() && otpDetailsList.size() >= otpPath.getNoOfAttempt()) {
                optionalOtpDetails = otpDetailsList.stream().findFirst();
                lastCreationDate = CommonUtils.getLocalDateTime(optionalOtpDetails.get().getCreationDate());
                long timeSinceLastAttempt = ChronoUnit.MILLIS.between(lastCreationDate, LocalDateTime.now());

                if (timeSinceLastAttempt < otpPath.getBlockTime()) {
                    long remainingTimeMinutes = (otpPath.getBlockTime() - timeSinceLastAttempt) / 60000;
                    throw new AlreadyExistsException(EXCEED_ATTEMPTS + TRY_AFTER + remainingTimeMinutes + MINUTES);
                }
            }
        }
        OtpDetails otpDetails = new OtpDetails();
        long otp = generateOTPSMS();
        logger.info("OTP generated: {}", otp);
        String message = MAIL_BODY.replaceFirst("\\{#var#}",
                String.valueOf(otp)).replaceFirst("\\{#var#}", String.valueOf(otpPath.getOtpExpiry()));
        try{
            String smsResponse = sendSMS(mobileNumber, message);
            otpDetails.setMobileNo(mobileNumber);
            otpDetails.setOtp((String.valueOf(otp)));
            otpDetails.setSmsResponse(message);
            otpDetailsRepository.save(otpDetails);
        }catch (Exception e){
            e.printStackTrace();
        }
        return otpDetails;
    }

    public boolean verifyOTP(String mobileNumber, String otp) {
        OTPData otpData = otpStore.get(mobileNumber);
        if (otpData != null) {
            long currentTime = Instant.now().toEpochMilli();
            if (currentTime - otpData.timestamp <= OTP_VALID_DURATION && otpData.otp.equals(otp)) {
                otpStore.remove(mobileNumber); // Remove OTP after successful verification
                return true;
            }
            // Remove expired OTP
            if (currentTime - otpData.timestamp > OTP_VALID_DURATION) {
                otpStore.remove(mobileNumber);
            }
        }
        return false;
    }


    private static class OTPData {
        final String otp;
        final long timestamp;

        OTPData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }
}

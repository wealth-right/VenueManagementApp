package com.venue.mgmt.services;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.time.Instant;

@Service
public class OTPService {
    private final Map<String, OTPData> otpStore = new HashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

    public String generateAndSendOTP(String mobileNumber) {
        String otp = generateOTP();
        otpStore.put(mobileNumber, new OTPData(otp, Instant.now().toEpochMilli()));
        
        // TODO: Integrate with actual SMS service
        // For now, just logging the OTP
        System.out.println("OTP sent to " + mobileNumber + ": " + otp);
        
        return otp;
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

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
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

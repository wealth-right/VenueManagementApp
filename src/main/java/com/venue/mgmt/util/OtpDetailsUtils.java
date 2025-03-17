package com.venue.mgmt.util;

import com.venue.mgmt.entities.OTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class OtpDetailsUtils {
    @Autowired
    private OTP otpPath;

    private static final Logger logger = LogManager.getLogger(OtpDetailsUtils.class);

    public long generateOTPSMS() {
        logger.info("NotificationServiceUtils - Inside generateOTPSMS method");
        Long length = otpPath.getOtpLength();
        long min = (long) Math.pow(10, length - 1);
        long max = (long) Math.pow(10, length) - 1;
        Random rndm_method = new Random();
        long otp = min + (long) (rndm_method.nextDouble() * (max - min));
        String otpStr = Long.toString(otp);
        if (otpStr.charAt(0) == '0') {
            otpStr = otpStr.substring(1);
            otp = Long.parseLong(otpStr);
        }
        return otp;
    }
    public String sendSMS(String smsNumber, String smsMessage) {
        logger.info("NotificationServiceUtils - Inside sendSMS method");
        String response = null;
        try {
            String url = String.format(
                    "%s?feedid=%s&username=%s&password=%s&To=%s&Text=%s&templateid=%s&entityid=%s&short=%s&async=%s&senderid=%s",
                    otpPath.getNetcore().getUrl(), otpPath.getNetcore().getFeedId(), otpPath.getNetcore().getUsername(), otpPath.getNetcore().getPassword(),
                    URLEncoder.encode(smsNumber, StandardCharsets.UTF_8),
                    URLEncoder.encode(smsMessage, StandardCharsets.UTF_8),
                    otpPath.getNetcore().getTemplateId(), otpPath.getNetcore().getEntityId(), otpPath.getNetcore().getShorts(),
                    otpPath.getNetcore().getAsync(), otpPath.getNetcore().getSenderId()
            );
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.getForObject(url, String.class);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}

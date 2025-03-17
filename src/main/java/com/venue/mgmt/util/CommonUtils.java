package com.venue.mgmt.util;

import com.venue.mgmt.entities.OTP;
import com.venue.mgmt.repositories.OtpDetailsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class CommonUtils {
    private static final Logger logger = LogManager.getLogger(CommonUtils.class);
    private static OtpDetailsRepository otpDetailsRepository;
    private static OTP otpPath;

    @Autowired
    public CommonUtils(OTP otpPath, OtpDetailsRepository otpDetailsRepository) {
        CommonUtils.otpPath = otpPath;
        CommonUtils.otpDetailsRepository = otpDetailsRepository;
    }

    public static String encodePassword(String password) {
        password = BCrypt.hashpw(password, BCrypt.gensalt());
        return password;
    }

    public static boolean checkPassword(String password, String dbPassword) {
        return BCrypt.checkpw(password, dbPassword);
    }

    public static LocalDateTime getLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static LocalDate getLocalDate(Date date) {
        if (date == null)
            return null;
        Instant instant = date.toInstant();
        return LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }

//    public static Boolean validateOtp(ValidateRequest validateRequest) {
//        logger.info("CommonUtils - Inside validateOtp method");
//        boolean checkedPassword = CommonUtils.checkPassword(String.valueOf(validateRequest.getOtp()), validateRequest.getOtpString());
//        if (!checkedPassword) {
//            return Boolean.FALSE;
//        }
//        if (ChronoUnit.MILLIS.between(getLocalDateTime(validateRequest.getCreationDate()), LocalDateTime.now()) > otpPath.getOtpExpiry()) {
//            throw new InvalidException(ErrorMsgConstants.ERROR_EXPIRED_SECURITY_CODE);
//        }
////            otpTrace.setIsValidated(Boolean.TRUE);
////            otpTraceRepository.save(otpTrace);
//        return Boolean.TRUE;
//    }

}

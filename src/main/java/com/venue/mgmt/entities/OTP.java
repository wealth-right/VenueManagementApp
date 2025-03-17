package com.venue.mgmt.entities;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties("customer-service.otp")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class OTP {
    @NotNull
    Long otpLength;
    @NotNull
    Long otpExpiry;
    @NotNull
    Long noOfAttempt;
    @NotNull
    Long otpResend;
    @NotNull
    Integer wrongOtpAttempt;
    @NotNull
    Long blockTime;
    @NotNull
    Netcore netcore = new Netcore();

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Netcore {
        @NotNull
        String url;
        @NotNull
        String username;
        @NotNull
        String password;
        @NotNull
        String feedId;
        @NotNull
        String templateId;
        @NotNull
        String entityId;
        @NotNull
        String shorts;
        @NotNull
        String async;
        @NotNull
        String senderId;
        @NotNull
        int connectTimeout;
        @NotNull
        int readTimeout;
    }
}
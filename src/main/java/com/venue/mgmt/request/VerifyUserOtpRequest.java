package com.venue.mgmt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

public class VerifyUserOtpRequest {
    
    @NotEmpty(message = "OTP cannot be empty")
    @JsonProperty("otp")
    private String otp;

    @NotEmpty(message = "OTP Screen cannot be empty")
    @JsonProperty("otpScreen")
    private String otpScreen;

    @NotEmpty(message = "Reference ID cannot be empty")
    @JsonProperty("refId")
    private String refId;

    @NotEmpty(message = "User ID cannot be empty")
    @JsonProperty("userId")
    private String userId;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getOtpScreen() {
        return otpScreen;
    }

    public void setOtpScreen(String otpScreen) {
        this.otpScreen = otpScreen;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

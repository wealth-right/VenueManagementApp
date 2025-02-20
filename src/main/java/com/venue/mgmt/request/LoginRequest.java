package com.venue.mgmt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter valid phone number")
    @NotEmpty(message = "Enter valid phone number")
    @JsonProperty("mobileNumber")
    String mobileNumber;

    @NotEmpty(message = "User type can't be empty.")
    @JsonProperty("loginUserType")
    String loginUserType;

    public String getLoginUserType() {
        return loginUserType;
    }

    public void setLoginUserType(String loginUserType) {
        this.loginUserType = loginUserType;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
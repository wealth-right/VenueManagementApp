package com.venue.mgmt.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidateOtpRequest {

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter valid phone number")
    @NotEmpty(message = "Enter valid phone number")
    private String mobileNumber;


    private String otp;

    @NotNull(message = "Enter valid leadId")
    private Long leadId;
}

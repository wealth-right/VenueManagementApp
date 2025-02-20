package com.venue.mgmt.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    String uuid;
    Long resendAttempt;

    public LoginResponse(String uuid, Long resendAttempt) {
        this.uuid = uuid;
        this.resendAttempt = resendAttempt;
    }

    public LoginResponse(String uuid) {
        this.uuid = uuid;
    }
}
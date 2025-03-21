package com.venue.mgmt.response;

import lombok.Data;

@Data
public class VerifyUserOtpResponse {
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private boolean response;

}
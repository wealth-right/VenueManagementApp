package com.venue.mgmt.dto;

import lombok.Data;

@Data
public class UserDetailsResponse {
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private UserDetails response;

    @Data
    public static class UserDetails {
        private String userId;
        private String firstName;
        private String lastName;
        private String mobileNumber;
        private String emailId;
        private String channelcode;
        private String branchCode;
        // Add other fields as necessary
    }
}
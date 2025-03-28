package com.venue.mgmt.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMasterRequest {
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String emailId;
    private String branchCode;
    private String channelCode;

}

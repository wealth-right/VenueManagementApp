package com.venue.mgmt.response;

import com.venue.mgmt.entities.LeadRegistration;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LeadResponse<T> {
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private LeadRegistration response;
}

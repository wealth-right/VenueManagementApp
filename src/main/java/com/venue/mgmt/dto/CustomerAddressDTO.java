package com.venue.mgmt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAddressDTO {
    private String addressType;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String cityName;
    private String stateName;
    private boolean status;
    private String countryName;
    private String pinCode;
    private String createdBy;
}

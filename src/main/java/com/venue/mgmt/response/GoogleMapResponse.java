package com.venue.mgmt.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleMapResponse<T> {
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private VenueSearchResponse response;
}

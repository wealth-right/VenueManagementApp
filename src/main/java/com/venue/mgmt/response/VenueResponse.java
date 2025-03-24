package com.venue.mgmt.response;

import com.venue.mgmt.entities.Venue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VenueResponse<T> {
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private Venue response;

}

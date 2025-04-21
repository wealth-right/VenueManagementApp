package com.venue.mgmt.response;

import com.venue.mgmt.dto.VenueDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VenueSearchResponse {
    private List<VenueDTO> venues;
    private String nextPageToken;
}

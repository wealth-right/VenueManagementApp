package com.venue.mgmt.response;

import com.venue.mgmt.entities.Venue;
import lombok.Data;

import java.util.List;

@Data
public class ApiResponse<T>{
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private List<?> response;
    private Venue venueDetails; // Add this field
    private PaginationDetails paginationDetails; // Add this field



}


package com.venue.mgmt.response;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Data
public class ApiResponse<T>{
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private List<?> response;


    private Venue venueDetails; // Add this field

    private PaginationDetails paginationDetails; // Add this field
//    public Venue getVenueDetails() {
//        return venueDetails;
//    }
//
//    public void setVenueDetails(Venue venueDetails) {
//        this.venueDetails = venueDetails;
//    }


//    public ApiResponse() {
//    }


//    public int getStatusCode() {
//        return statusCode;
//    }
//
//    public void setStatusCode(int statusCode) {
//        this.statusCode = statusCode;
//    }
//
//    public String getStatusMsg() {
//        return statusMsg;
//    }
//
//    public void setStatusMsg(String statusMsg) {
//        this.statusMsg = statusMsg;
//    }
//
//    public String getErrorMsg() {
//        return errorMsg;
//    }
//
//    public void setErrorMsg(String errorMsg) {
//        this.errorMsg = errorMsg;
//    }
//
//    public List<?> getResponse() {
//        return response;
//    }
//
//    public void setResponse(List<?> response) {
//        this.response = response;
//    }



}


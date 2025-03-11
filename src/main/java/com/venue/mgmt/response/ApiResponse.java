package com.venue.mgmt.response;

import com.venue.mgmt.entities.LeadRegistration;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ApiResponse<T>{
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private List<?> response;


    public ApiResponse() {
    }

    public ApiResponse(int statusCode, String statusMsg, String errorMsg, List<?> response) {
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
        this.errorMsg = errorMsg;
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<?> getResponse() {
        return response;
    }

    public void setResponse(List<?> response) {
        this.response = response;
    }
}


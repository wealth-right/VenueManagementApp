package com.venue.mgmt.response;

public class ErrorResponse {
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private Object response;

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

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public ErrorResponse(int statusCode, String statusMsg, String errorMsg, Object response) {
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
        this.errorMsg = errorMsg;
        this.response = response;
    }
}
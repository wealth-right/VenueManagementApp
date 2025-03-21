package com.venue.mgmt.response;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponse<T> {
    private int statusCode;
    private String statusMsg;
    private String errorMsg;
    private List<?> response;
    private PaginationDetails pagination; // Add this field



}


//package com.venue.mgmt.dto;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Data;
//
//@Data
//@Schema(description = "Search criteria for leads")
//public class LeadSearchCriteria {
//    @Schema(description = "Full name of the lead (case-insensitive, partial match)")
//    private String fullName;
//
//    @Schema(description = "Email of the lead (case-insensitive, partial match)")
//    private String email;
//
//    @Schema(description = "Mobile number of the lead (partial match)")
//    private String mobile;
//
//    @Schema(description = "Field to sort by (fullName, email, mobileNumber, createdDate)", defaultValue = "fullName")
//    private String sortBy = "fullName";
//
//    @Schema(description = "Sort direction (asc or desc)", defaultValue = "asc")
//    private String sortDirection = "asc";
//
//    @Schema(description = "Page number (0-based)", defaultValue = "0")
//    private int page = 0;
//
//    @Schema(description = "Number of records per page", defaultValue = "10")
//    private int size = 10;
//
//    public String getFullName() {
//        return fullName;
//    }
//
//    public void setFullName(String fullName) {
//        this.fullName = fullName;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getMobile() {
//        return mobile;
//    }
//
//    public void setMobile(String mobile) {
//        this.mobile = mobile;
//    }
//
//    public String getSortBy() {
//        return sortBy;
//    }
//
//    public void setSortBy(String sortBy) {
//        this.sortBy = sortBy;
//    }
//
//    public String getSortDirection() {
//        return sortDirection;
//    }
//
//    public void setSortDirection(String sortDirection) {
//        this.sortDirection = sortDirection;
//    }
//
//    public int getPage() {
//        return page;
//    }
//
//    public void setPage(int page) {
//        this.page = page;
//    }
//
//    public int getSize() {
//        return size;
//    }
//
//    public void setSize(int size) {
//        this.size = size;
//    }
//
//    public LeadSearchCriteria() {
//    }
//}

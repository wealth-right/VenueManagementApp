package com.venue.mgmt.response;

import lombok.Data;

@Data
public class PaginationDetails {
    private int currentPage;
    private long totalRecords;
    private int totalPages;

}

package com.venue.mgmt.enums;

public enum ProductType {
    MUTUAL_FUND("Mutual Fund"),
    INSURANCE("Insurance"),
    FIXED_DEPOSIT("Fixed Deposit"),
    LOANS("Loans");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

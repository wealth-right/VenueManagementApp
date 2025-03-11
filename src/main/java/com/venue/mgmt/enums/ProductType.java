package com.venue.mgmt.enums;

public enum ProductType {
    MUTUAL_FUND("Mutual Funds"),
    HEALTH_INSURANCE("Health Insurance"),
    TERM_INSURANCE("Term Insurance"),
    LOANS("Loans");



    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

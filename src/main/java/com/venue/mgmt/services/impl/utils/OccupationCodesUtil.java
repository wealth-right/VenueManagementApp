package com.venue.mgmt.services.impl.utils;

public class OccupationCodesUtil {

    public static String mapOccupationToCode(String occupation) {
        switch (occupation) {
            case "Business":
                return "01";
            case "Services":
                return "02";
            case "Professional":
                return "03";
            case "Agriculture":
                return "04";
            case "Retired":
                return "05";
            case "Housewife":
                return "06";
            case "Student":
                return "07";
            case "Others":
                return "08";
            default:
                return "08"; // Default to "Others" if no match found
        }
    }
}

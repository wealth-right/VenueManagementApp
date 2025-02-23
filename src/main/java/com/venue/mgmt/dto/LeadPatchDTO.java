package com.venue.mgmt.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class LeadPatchDTO {
    private String name;
    private String email;
    private String mobileNumber;
    private String eventType;
    private LocalDate eventDate;
    private Integer numberOfGuests;
    private BigDecimal budget;
    private String requirements;
    private String status;
    private String campaign;

    // Helper method to check if a field is present in the patch request
    public boolean hasField(String fieldName) {
        try {
            return this.getClass().getDeclaredField(fieldName).get(this) != null;
        } catch (Exception e) {
            return false;
        }
    }

}

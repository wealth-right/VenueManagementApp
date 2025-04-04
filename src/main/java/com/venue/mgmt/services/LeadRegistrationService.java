package com.venue.mgmt.services;

import com.venue.mgmt.entities.LeadRegistration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public interface LeadRegistrationService {
    LeadRegistration saveLead(LeadRegistration leadRegistration);
    Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedByAndIsDeletedFalse(String sortDirection, int page, int size, String userId);

    Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRangeAndIsDeletedFalse(String sortDirection,
                                                                                             int page, int size, String userId,
                                                                                             Long venueId, Date startDate, Date endDate);
    List<LeadRegistration> simpleSearchLeads(String searchTerm,String userId);
    LeadRegistration updateLead(Long leadId, LeadRegistration leadRegistration,String authHeader);
    void deleteLead(Long leadId,String authHeader);
}

package com.venue.mgmt.services;

import com.venue.mgmt.entities.LeadRegistration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public interface LeadRegistrationService {
    LeadRegistration saveLead(LeadRegistration leadRegistration);
    LeadRegistration getLeadById(Long leadId);
    Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedBy(String sortDirection, int page, int size, String userId);

    Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRange(String sortDirection, int page, int size, String userId, Long venueId, Date startDate, Date endDate);
    List<LeadRegistration> simpleSearchLeads(String searchTerm,String userId);
    LeadRegistration updateLead(Long leadId, LeadRegistration leadRegistration);
    void deleteLead(Long leadId);
}

package com.venue.mgmt.services;

import com.venue.mgmt.dto.LeadPatchDTO;
import com.venue.mgmt.dto.LeadSearchCriteria;
import com.venue.mgmt.entities.LeadRegistration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LeadRegistrationService {
    LeadRegistration saveLead(LeadRegistration leadRegistration);
    Page<LeadRegistration> getAllLeadsSortedByCreationDate(String sortDirection, int page, int size);
    List<LeadRegistration> simpleSearchLeads(String searchTerm);
    LeadRegistration updateLead(Long leadId, LeadRegistration leadRegistration);
    void deleteLead(Long leadId);
}

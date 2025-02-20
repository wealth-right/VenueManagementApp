package com.venue.mgmt.services;

import com.venue.mgmt.dto.LeadSearchCriteria;
import com.venue.mgmt.entities.LeadRegistration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LeadRegistrationService {
    LeadRegistration saveLead(LeadRegistration leadRegistration);
    List<LeadRegistration> getAllLeadsSortedByCreationDate(String sortDirection);
    LeadRegistration getLeadByFullName(String fullName);
    Page<LeadRegistration> searchLeads(LeadSearchCriteria criteria);
}

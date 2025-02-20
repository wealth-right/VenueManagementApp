package com.venue.mgmt.services.impl;

import com.venue.mgmt.dto.LeadSearchCriteria;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.exception.LeadNotFoundException;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.services.LeadRegistrationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.venue.mgmt.constant.GeneralMsgConstants.LEAD_WITH_GIVEN;
import static com.venue.mgmt.constant.GeneralMsgConstants.IS_NOT_FOUND;

@Service
@Slf4j
public class LeadRegistrationServiceImpl implements LeadRegistrationService {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationServiceImpl.class);
    
    @Autowired
    private LeadRegRepository leadRegRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public LeadRegistration saveLead(LeadRegistration leadRegistration) {
        try {
            return leadRegRepository.save(leadRegistration);
        } catch (Exception e) {
            logger.error("Error while saving lead: {}", e.getMessage(), e);
            throw e;
        }
    }




    @Override
    @Transactional(readOnly = true)
    public List<LeadRegistration> getAllLeadsSortedByCreationDate(String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, "creationDate");
            return leadRegRepository.findAll(sort);
        } catch (Exception e) {
            logger.error("Error while fetching leads: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LeadRegistration getLeadByFullName(String fullName) {
        return leadRegRepository.findByFullName(fullName)
            .orElseThrow(() -> new LeadNotFoundException(LEAD_WITH_GIVEN+ fullName+IS_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadRegistration> searchLeads(LeadSearchCriteria criteria) {
        logger.info("Searching leads with criteria: {}", criteria);

        try {
            // Convert Java property names to database column names
            String sortField = criteria.getSortBy();
            if (sortField == null || sortField.isEmpty() || sortField.equals("fullName")) {
                sortField = "full_name";
            } else if (sortField.equals("mobileNumber")) {
                sortField = "mobile_number";
            } else if (sortField.equals("createdDate")) {
                sortField = "created_date";
            }
            
            String direction = (criteria.getSortDirection() != null && 
                              criteria.getSortDirection().equalsIgnoreCase("desc")) ? "DESC" : "ASC";
            
            logger.info("Using sort field: {}, direction: {}", sortField, direction);
            
            int page = Math.max(0, criteria.getPage());
            int size = Math.max(1, criteria.getSize());
            long offset = (long) page * size;
            
            // Get total count
            long total = leadRegRepository.countSearchResults(
                criteria.getFullName(),
                criteria.getEmail(),
                criteria.getMobile()
            );

            logger.info("Total matching records: {}", total);
            
            if (total == 0) {
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
            }
            
            // Get paginated results
            List<LeadRegistration> results = leadRegRepository.searchLeadsWithoutPaging(
                criteria.getFullName(),
                criteria.getEmail(),
                criteria.getMobile(),
                sortField,
                direction,
                size,
                offset
            );
            
            logger.info("Found {} results for page {} with size {}", results.size(), page, size);
            
            return new PageImpl<>(results, PageRequest.of(page, size), total);
            
        } catch (Exception e) {
            logger.error("Error while searching leads: {}", e.getMessage(), e);
            throw e;
        }
    }
}

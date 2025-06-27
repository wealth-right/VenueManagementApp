package com.venue.mgmt.services.impl;

import com.venue.mgmt.entities.ActivityType;
import com.venue.mgmt.repositories.ActivityTypeRepository;
import com.venue.mgmt.services.ActivityTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityTypeServiceImpl implements ActivityTypeService {

    private final ActivityTypeRepository activityTypeRepository;

    public ActivityTypeServiceImpl(ActivityTypeRepository activityTypeRepository) {
        this.activityTypeRepository = activityTypeRepository;
    }

    @Override
    public List<ActivityType> getAllActivityTypes() {
        return activityTypeRepository.findAll();
    }
}

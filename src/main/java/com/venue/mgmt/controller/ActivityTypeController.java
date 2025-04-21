package com.venue.mgmt.controller;

import com.venue.mgmt.entities.ActivityType;
import com.venue.mgmt.response.ApiResponse;
import com.venue.mgmt.services.ActivityTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.venue.mgmt.constant.GeneralMsgConstants.SUCCESS;

@RestController
@RequestMapping("/venue-app/v1")
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;

    public ActivityTypeController(ActivityTypeService activityTypeService) {
        this.activityTypeService = activityTypeService;
    }

    @GetMapping("/activity-types")
    public ResponseEntity<ApiResponse<List<ActivityType>>> getAllActivityTypes() {
        ApiResponse<List<ActivityType>> response = new ApiResponse<>();
        try {
            List<ActivityType> activityTypes = activityTypeService.getAllActivityTypes();
            response.setStatusCode(200);
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(activityTypes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setStatusMsg("Failure");
            response.setErrorMsg("An error occurred while fetching activity types: " + e.getMessage());
            response.setResponse(null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
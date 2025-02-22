package com.venue.mgmt.controller;

import com.venue.mgmt.entities.Campaign;
import com.venue.mgmt.services.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/venue-app/v1/campaigns")
@Tag(name = "Campaign Controller", description = "API to manage campaigns")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;


    @GetMapping
    @Operation(summary = "Get all campaigns", description = "Retrieves all campaigns")
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        List<Campaign> campaigns = campaignService.getAllCampaigns();
        return ResponseEntity.ok(campaigns);
    }

}

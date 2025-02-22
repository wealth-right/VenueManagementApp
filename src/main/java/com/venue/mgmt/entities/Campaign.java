package com.venue.mgmt.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Entity
@Table(name = "campaigns")
public class Campaign {

    private static final Logger logger = LogManager.getLogger(Campaign.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignId;

    @NotBlank(message = "Campaign name is required")
    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    @JsonBackReference
    private LeadRegistration leadRegistration;

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public LeadRegistration getLeadRegistration() {
        return leadRegistration;
    }

    public void setLeadRegistration(LeadRegistration leadRegistration) {
        logger.info("Setting leadRegistration for campaign '{}'. Lead ID: {}", 
            this.campaignName, 
            leadRegistration != null ? leadRegistration.getLeadId() : "null");
        this.leadRegistration = leadRegistration;
    }

    @PrePersist
    public void logNewCampaign() {
        logger.info("About to persist new campaign: {}", this.campaignName);
    }

    @PostPersist
    public void logAfterPersist() {
        logger.info("Campaign persisted successfully. ID: {}, Name: {}", this.campaignId, this.campaignName);
    }
}

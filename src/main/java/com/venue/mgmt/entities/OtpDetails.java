package com.venue.mgmt.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "otp_details", schema = "leadmgmt")
public class OtpDetails extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "otp_seq")
    @SequenceGenerator(name = "otp_seq", sequenceName = "otp_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "mobile_no", nullable = false)
    private String mobileNo;

    @Column(name = "otp", nullable = false)
    private String otp;

    @Column(nullable = false)
    private int attempts;

    @Column(name="sms_response", nullable = false)
    private String smsResponse;


    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name="lead_id",nullable = false)
    private Long leadId;


    public OtpDetails() {
    }
    public OtpDetails(Long id, String mobileNo, String otp, int attempts, boolean isVerified,String smsResponse,Long leadId) {
        this.id = id;
        this.mobileNo = mobileNo;
        this.otp = otp;
        this.attempts = attempts;
        this.isVerified = isVerified;
        this.leadId = leadId;
        this.smsResponse = smsResponse;
    }
    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }


    public String getSmsResponse() {
        return smsResponse;
    }

    public void setSmsResponse(String smsResponse) {
        this.smsResponse = smsResponse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}

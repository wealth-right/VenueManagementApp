package com.venue.mgmt.repositories;

import com.venue.mgmt.entities.OtpDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface OtpDetailsRepository extends JpaRepository<OtpDetails, Long> {
    List<OtpDetails> findByMobileNo(String mobileNo);

    List<OtpDetails> findByLeadIdAndMobileNo(Long leadId, String mobileNo);


}

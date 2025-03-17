package com.venue.mgmt.repositories;

import com.venue.mgmt.entities.OtpDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtpDetailsRepository extends JpaRepository<OtpDetails, Long> {
    List<OtpDetails> findByMobileNo(String mobileNo);

}

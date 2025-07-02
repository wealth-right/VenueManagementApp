package com.venue.mgmt.repositories;

import com.venue.mgmt.entities.LeadDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeadDetailsRepository extends JpaRepository<LeadDetailsEntity,Long> {

    @Query("SELECT l FROM LeadDetailsEntity l WHERE l.stage NOT IN (:excludedStages)")
    List<LeadDetailsEntity> findByStageNotIn(@Param("excludedStages") List<String> excludedStages);


    @Modifying
    @Query(
            "UPDATE LeadDetailsEntity l SET l.score = :score, l.temperature = :temperature WHERE l.id = :id")
    void updatedTemperature(
            @Param("id") Long id, @Param("score") int score, @Param("temperature") String temperature);
}

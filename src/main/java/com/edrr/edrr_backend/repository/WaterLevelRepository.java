package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.WaterLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterLevelRepository extends MongoRepository<WaterLevel, String> {

    // Find by location name
    Optional<WaterLevel> findByLocationName(String locationName);

    // Find all by district
    List<WaterLevel> findByDistrict(String district);

    // Find all critical water levels (for alerts)
    List<WaterLevel> findByStatus(String status);

    // Find levels above a percentage (e.g., all above 80%)
    List<WaterLevel> findByPercentageOfMaxGreaterThan(double percentage);
}
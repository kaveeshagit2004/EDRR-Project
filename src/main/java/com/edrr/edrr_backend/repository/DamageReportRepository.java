package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.DamageReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DamageReportRepository extends MongoRepository<DamageReport, String> {

    // Get reports by status
    List<DamageReport> findByStatus(String status);

    // Get reports by damage type
    List<DamageReport> findByDamageType(String damageType);

    // Get reports by severity
    List<DamageReport> findBySeverity(String severity);

    // Get reports submitted by a specific officer
    List<DamageReport> findByReportedBy(String userId);

    // Find by report number (e.g., RPT-2024-001)
    Optional<DamageReport> findByReportNumber(String reportNumber);

    // Count reports by status (for dashboard stats)
    long countByStatus(String status);

    // Get reports by location keyword
    List<DamageReport> findByLocationContainingIgnoreCase(String location);
}
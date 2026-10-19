package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.DamageReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DamageReportRepository extends MongoRepository<DamageReport, String> {

    // Active (non-deleted) reports
    @Query("{ '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }")
    List<DamageReport> findAllActive();

    // Soft-deleted reports (in recycle bin)
    @Query("{ 'deleted': true }")
    List<DamageReport> findByDeletedTrue();

    // Active reports submitted by a specific officer
    @Query("{ 'reportedBy': ?0, '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }")
    List<DamageReport> findByReportedByAndNotDeleted(String userId);

    // Active reports by status
    @Query("{ 'status': ?0, '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }")
    List<DamageReport> findByStatusAndNotDeleted(String status);

    // Get reports by status
    List<DamageReport> findByStatus(String status);

    // Get reports by damage type
    List<DamageReport> findByDamageType(String damageType);

    // Get reports by severity
    List<DamageReport> findBySeverity(String severity);

    // Get reports submitted by a specific officer (legacy)
    List<DamageReport> findByReportedBy(String userId);

    // Find by report number (e.g., RPT-2024-001)
    Optional<DamageReport> findByReportNumber(String reportNumber);

    // Count reports by status (for dashboard stats)
    long countByStatus(String status);

    @Query(value = "{ '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }", count = true)
    long countActive();

    @Query(value = "{ 'deleted': true }", count = true)
    long countByDeletedTrue();

    @Query(value = "{ 'status': ?0, '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }", count = true)
    long countByStatusAndNotDeleted(String status);

    // Get reports by location keyword
    List<DamageReport> findByLocationContainingIgnoreCase(String location);
}
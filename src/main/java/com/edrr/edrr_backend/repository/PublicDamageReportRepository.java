package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.PublicDamageReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PublicDamageReportRepository extends MongoRepository<PublicDamageReport, String> {

    // Active (non-deleted) public reports
    @Query("{ '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }")
    List<PublicDamageReport> findAllActive();

    // Active public reports created within cutoff date/time
    @Query("{ '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ], 'reportedAt': { '$gte': ?0 } }")
    List<PublicDamageReport> findAllActiveSince(LocalDateTime cutoff);

    // Soft-deleted reports
    @Query("{ 'deleted': true }")
    List<PublicDamageReport> findByDeletedTrue();

    // Active reports by status
    @Query("{ 'status': ?0, '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }")
    List<PublicDamageReport> findByStatusAndNotDeleted(String status);

    Optional<PublicDamageReport> findByReportNumber(String reportNumber);

    @Query(value = "{ '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }", count = true)
    long countActive();

    @Query(value = "{ '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ], 'reportedAt': { '$gte': ?0 } }", count = true)
    long countActiveSince(LocalDateTime cutoff);

    @Query(value = "{ 'deleted': true }", count = true)
    long countByDeletedTrue();

    @Query(value = "{ 'status': ?0, '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }", count = true)
    long countByStatusAndNotDeleted(String status);

    @Query(value = "{ 'viewedByRda': true, '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }", count = true)
    long countByViewedByRdaTrueAndNotDeleted();

    @Query(value = "{ '$or': [ { 'viewedByRda': false }, { 'viewedByRda': { '$exists': false } } ], '$and': [ { '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] } ] }", count = true)
    long countByViewedByRdaFalseAndNotDeleted();
}

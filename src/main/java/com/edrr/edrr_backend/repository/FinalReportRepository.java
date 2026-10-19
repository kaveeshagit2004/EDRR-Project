package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.FinalReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinalReportRepository extends MongoRepository<FinalReport, String> {

    // Retrieve all final reports ordered with newest first
    List<FinalReport> findAllByOrderByUploadedAtDesc();

    // Find by damage type if needed
    List<FinalReport> findByDamageTypeOrderByUploadedAtDesc(String damageType);
}

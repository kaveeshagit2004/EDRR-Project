package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.AlertAnnouncement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertAnnouncementRepository extends MongoRepository<AlertAnnouncement, String> {
    List<AlertAnnouncement> findAllByOrderByCreatedAtDesc();
    List<AlertAnnouncement> findByActiveTrueOrderByCreatedAtDesc();
}

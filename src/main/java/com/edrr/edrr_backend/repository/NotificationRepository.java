package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findByActiveTrueOrderByCreatedAtDesc();
}

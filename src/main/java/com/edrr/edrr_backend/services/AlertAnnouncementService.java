package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.model.AlertAnnouncement;
import com.edrr.edrr_backend.model.Notification;
import com.edrr.edrr_backend.repository.AlertAnnouncementRepository;
import com.edrr.edrr_backend.repository.NotificationRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlertAnnouncementService {

    private final AlertAnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/stored_announcements/";

    public AlertAnnouncementService(AlertAnnouncementRepository announcementRepository, NotificationRepository notificationRepository) {
        this.announcementRepository = announcementRepository;
        this.notificationRepository = notificationRepository;
        ensureDirectoryExists(UPLOAD_DIR);
    }

    private void ensureDirectoryExists(String dirPath) {
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println("Could not create directory: " + dirPath);
        }
    }

    public AlertAnnouncement createAnnouncement(String title,
                                                String message,
                                                String alertLevel,
                                                String targetAudience,
                                                MultipartFile image,
                                                String senderEmail,
                                                String senderName) throws IOException {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Announcement title is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Announcement message is required");
        }

        AlertAnnouncement announcement = new AlertAnnouncement();
        announcement.setTitle(title.trim());
        announcement.setMessage(message.trim());
        announcement.setAlertLevel(alertLevel != null && !alertLevel.isBlank() ? alertLevel.trim().toUpperCase() : "WARNING");
        announcement.setTargetAudience(targetAudience != null && !targetAudience.isBlank() ? targetAudience.trim().toUpperCase() : "ALL");
        announcement.setSenderEmail(senderEmail != null ? senderEmail.trim() : "ga@edrr.com");
        announcement.setSenderName(senderName != null && !senderName.isBlank() ? senderName.trim() : "Government Agent Admin");
        announcement.setSenderRole("GA_ADMIN");
        announcement.setActive(true);
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            ensureDirectoryExists(UPLOAD_DIR);
            String originalName = image.getOriginalFilename();
            String cleanOriginalName = originalName != null ? Paths.get(originalName).getFileName().toString() : "announcement_image.jpg";
            String uniqueStoredName = "announcement_" + UUID.randomUUID().toString() + "_" + cleanOriginalName.replaceAll("[^a-zA-Z0-9._-]", "_");

            Path targetPath = Paths.get(UPLOAD_DIR).resolve(uniqueStoredName).normalize();
            Files.copy(image.getInputStream(), targetPath);

            announcement.setStoredFileName(uniqueStoredName);
            announcement.setImageUrl("http://localhost:8080/api/announcements/images/" + uniqueStoredName);
        }

        AlertAnnouncement saved = announcementRepository.save(announcement);

        // Also broadcast notification for all users and citizens
        try {
            Notification notification = new Notification();
            notification.setTitle("Alert Announcement: " + saved.getTitle());
            notification.setMessage(saved.getMessage());
            notification.setType("ALERT_ANNOUNCEMENT");
            notification.setPriority(saved.getAlertLevel());
            notification.setTargetRoles(List.of("ALL", "CITIZENS", "OFFICERS", "ADMINS", "RURAL_OFFICER", "IRRIGATION_OFFICER", "RDA_ADMIN", "GA_ADMIN"));
            notification.setRelatedReportId(saved.getId());
            notification.setActive(true);
            notification.setCreatedBy(saved.getSenderName());
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Failed to create matching notification record: " + e.getMessage());
        }

        return saved;
    }

    public List<AlertAnnouncement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AlertAnnouncement> getActiveAnnouncements() {
        return announcementRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public void deleteAnnouncement(String id, String userRole) {
        if (userRole != null && !userRole.equalsIgnoreCase("GA_ADMIN") && !userRole.equalsIgnoreCase("ROLE_GA_ADMIN")) {
            throw new RuntimeException("Only GA Admin is authorized to delete alert announcements");
        }

        AlertAnnouncement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));

        // Delete disk image if stored
        if (announcement.getStoredFileName() != null && !announcement.getStoredFileName().isBlank()) {
            try {
                Path filePath = Paths.get(UPLOAD_DIR).resolve(announcement.getStoredFileName()).normalize();
                File f = filePath.toFile();
                if (f.exists()) {
                    f.delete();
                }
            } catch (Exception e) {
                System.err.println("Could not delete announcement image file: " + e.getMessage());
            }
        }

        announcementRepository.deleteById(id);
    }

    public Resource loadImageAsResource(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                // Secondary fallback
                Path secondary = Paths.get("stored_announcements").resolve(filename).toAbsolutePath().normalize();
                Resource secRes = new UrlResource(secondary.toUri());
                if (secRes.exists() && secRes.isReadable()) {
                    return secRes;
                }
                throw new RuntimeException("Image file not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed file path for image: " + filename, e);
        }
    }
}

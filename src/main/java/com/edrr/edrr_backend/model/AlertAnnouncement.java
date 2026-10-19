package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "alert_announcements")
public class AlertAnnouncement {

    @Id
    private String id;

    private String title;
    private String message;

    // Alert Level: CRITICAL, WARNING, INFO
    private String alertLevel = "WARNING";

    // Target Audience: ALL, CITIZENS, OFFICERS
    private String targetAudience = "ALL";

    // URL to access the uploaded announcement image
    private String imageUrl;

    // Stored disk filename
    private String storedFileName;

    // Sender metadata
    private String senderName;
    private String senderEmail;
    private String senderRole = "GA_ADMIN";

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AlertAnnouncement() {
    }

    public AlertAnnouncement(String title, String message, String alertLevel, String targetAudience, String imageUrl, String storedFileName, String senderName, String senderEmail, String senderRole) {
        this.title = title;
        this.message = message;
        this.alertLevel = alertLevel != null ? alertLevel : "WARNING";
        this.targetAudience = targetAudience != null ? targetAudience : "ALL";
        this.imageUrl = imageUrl;
        this.storedFileName = storedFileName;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderRole = senderRole != null ? senderRole : "GA_ADMIN";
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

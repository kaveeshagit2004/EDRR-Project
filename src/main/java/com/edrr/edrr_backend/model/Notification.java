package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String title;
    private String message;

    // Type: FLOOD_WARNING, DAMAGE_REPORT, STATUS_UPDATE, SYSTEM
    private String type;

    // Priority: LOW, MEDIUM, HIGH, CRITICAL
    private String priority;

    // Who can see this: ALL, CITIZENS, OFFICERS, ADMINS
    private List<String> targetRoles;

    private String relatedReportId;   // Link to a damage report if relevant
    private String relatedLocationId; // Link to water level location

    private boolean active = true;

    private String createdBy;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(String id, String title, String message, String type, String priority, List<String> targetRoles, String relatedReportId, String relatedLocationId, boolean active, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.targetRoles = targetRoles;
        this.relatedReportId = relatedReportId;
        this.relatedLocationId = relatedLocationId;
        this.active = active;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public List<String> getTargetRoles() { return targetRoles; }
    public void setTargetRoles(List<String> targetRoles) { this.targetRoles = targetRoles; }
    public String getRelatedReportId() { return relatedReportId; }
    public void setRelatedReportId(String relatedReportId) { this.relatedReportId = relatedReportId; }
    public String getRelatedLocationId() { return relatedLocationId; }
    public void setRelatedLocationId(String relatedLocationId) { this.relatedLocationId = relatedLocationId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
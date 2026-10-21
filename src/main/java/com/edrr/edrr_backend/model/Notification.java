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

    // Type: FLOOD_WARNING, DAMAGE_REPORT, STATUS_UPDATE, SYSTEM, CITIZEN_REPORT
    private String type;

    // Priority: LOW, MEDIUM, HIGH, CRITICAL
    private String priority;

    // Who can see this: RURAL_OFFICER, RDA_ADMIN
    private List<String> targetRoles;

    // Rural Division Name (e.g. "Thihagoda") for routing to relevant rural officer
    private String targetDivision;

    private String relatedReportId;   // Link to a damage report or public report
    private String reportNumber;      // e.g. "PUB-2026-0001"
    private String location;
    private String reporterName;
    private String relatedLocationId; // Link to water level location

    private boolean active = true;

    private String createdBy;
    private LocalDateTime createdAt;

    // List of user IDs who have read/dismissed this notification
    private List<String> readByUsers;

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

    public String getTargetDivision() { return targetDivision; }
    public void setTargetDivision(String targetDivision) { this.targetDivision = targetDivision; }

    public String getRelatedReportId() { return relatedReportId; }
    public void setRelatedReportId(String relatedReportId) { this.relatedReportId = relatedReportId; }

    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getRelatedLocationId() { return relatedLocationId; }
    public void setRelatedLocationId(String relatedLocationId) { this.relatedLocationId = relatedLocationId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getReadByUsers() { return readByUsers; }
    public void setReadByUsers(List<String> readByUsers) { this.readByUsers = readByUsers; }
}

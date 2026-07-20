package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "damage_reports")
public class DamageReport {

    @Id
    private String id;

    // Auto-generated report number like "RPT-2024-001"
    private String reportNumber;

    @NotBlank(message = "Title is required")
    private String title;

    // Type: FLOOD, ROAD_DAMAGE, LANDSLIDE, BRIDGE_DAMAGE, BUILDING, WATER_POLLUTION, OTHER
    @NotBlank(message = "Damage type is required")
    private String damageType;

    @NotBlank(message = "Location is required")
    private String location;

    private double latitude;   // GPS coordinates
    private double longitude;

    // Severity: LOW, MEDIUM, HIGH, CRITICAL
    private String severity;

    private String description;

    // Estimated area affected (in square meters)
    private String affectedArea;

    // List of photo URLs/paths
    private java.util.List<String> photoUrls;

    // Status: PENDING, IN_PROGRESS, RESOLVED, REJECTED
    private String status = "PENDING";

    // Recovery progress 0-100%
    private int recoveryProgress = 0;

    // Who reported it
    private String reportedBy;      // User ID of the officer
    private String reporterName;    // Name for display

    // Admin actions
    private String assignedOfficer;
    private String adminNotes;
    private String rejectionReason;

    private LocalDateTime reportedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public DamageReport() {
    }

    public DamageReport(String id, String reportNumber, String title, String damageType, String location, double latitude, double longitude, String severity, String description, String affectedArea, java.util.List<String> photoUrls, String status, int recoveryProgress, String reportedBy, String reporterName, String assignedOfficer, String adminNotes, String rejectionReason, LocalDateTime reportedAt, LocalDateTime updatedAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.reportNumber = reportNumber;
        this.title = title;
        this.damageType = damageType;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.severity = severity;
        this.description = description;
        this.affectedArea = affectedArea;
        this.photoUrls = photoUrls;
        this.status = status;
        this.recoveryProgress = recoveryProgress;
        this.reportedBy = reportedBy;
        this.reporterName = reporterName;
        this.assignedOfficer = assignedOfficer;
        this.adminNotes = adminNotes;
        this.rejectionReason = rejectionReason;
        this.reportedAt = reportedAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAffectedArea() { return affectedArea; }
    public void setAffectedArea(String affectedArea) { this.affectedArea = affectedArea; }
    public java.util.List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(java.util.List<String> photoUrls) { this.photoUrls = photoUrls; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRecoveryProgress() { return recoveryProgress; }
    public void setRecoveryProgress(int recoveryProgress) { this.recoveryProgress = recoveryProgress; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(String assignedOfficer) { this.assignedOfficer = assignedOfficer; }
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
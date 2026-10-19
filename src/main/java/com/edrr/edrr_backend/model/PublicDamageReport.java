package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "public_damage_reports")
public class PublicDamageReport {

    @Id
    private String id;

    // Auto-generated public report number like "PUB-2026-0001"
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

    // Rural Officer Division Name (used to route and filter reports for rural officers)
    private String divisionName;

    // Citizen submitter details
    private String reporterName;
    private String reporterNic;
    private String reporterPhone;
    private String reporterEmail;

    // List of photo URLs/paths
    private List<String> photoUrls;

    // Status: PENDING, IN_PROGRESS, RESOLVED, REJECTED
    private String status = "PENDING";

    // Recovery progress 0-100%
    private int recoveryProgress = 0;

    // Admin notes
    private String adminNotes;
    private String assignedOfficer;

    private LocalDateTime reportedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // Soft delete / recycle bin
    private boolean deleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private String deletedByName;

    public PublicDamageReport() {
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

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterNic() { return reporterNic; }
    public void setReporterNic(String reporterNic) { this.reporterNic = reporterNic; }

    public String getReporterPhone() { return reporterPhone; }
    public void setReporterPhone(String reporterPhone) { this.reporterPhone = reporterPhone; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRecoveryProgress() { return recoveryProgress; }
    public void setRecoveryProgress(int recoveryProgress) { this.recoveryProgress = recoveryProgress; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public String getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(String assignedOfficer) { this.assignedOfficer = assignedOfficer; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public String getDeletedByName() { return deletedByName; }
    public void setDeletedByName(String deletedByName) { this.deletedByName = deletedByName; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }
}

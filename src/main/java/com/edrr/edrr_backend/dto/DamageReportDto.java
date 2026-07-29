package com.edrr.edrr_backend.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class DamageReportDto {

    private String id;

    private String title;

    // Type: FLOOD, ROAD_DAMAGE, LANDSLIDE, BRIDGE_DAMAGE, BUILDING, WATER_POLLUTION, OTHER
    private String damageType;

    private String location;

    private double latitude;   // GPS coordinates
    private double longitude;

    // Severity: LOW, MEDIUM, HIGH, CRITICAL
    private String severity;

    private String description;

    // Estimated area affected (in square meters)
    private String affectedArea;

    private List<MultipartFile> photos;

    private String reportedBy;
    private String reporterName;

    public DamageReportDto() {
    }

    public DamageReportDto(String id, String title, String damageType, String location, double latitude, double longitude, String severity, String description, String affectedArea, List<MultipartFile> photos, String reportedBy, String reporterName) {
        this.id = id;
        this.title = title;
        this.damageType = damageType;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.severity = severity;
        this.description = description;
        this.affectedArea = affectedArea;
        this.photos = photos;
        this.reportedBy = reportedBy;
        this.reporterName = reporterName;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAffectedArea() {
        return affectedArea;
    }

    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }

    public List<MultipartFile> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MultipartFile> photos) {
        this.photos = photos;
    }
}

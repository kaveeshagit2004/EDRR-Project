package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "water_levels")
public class WaterLevel {

    @Id
    private String id;

    // Location name e.g., "Nilwala River - Matara"
    private String locationName;

    private String district;   // e.g., "Matara"

    private double currentLevel;   // in meters
    private double maximumLimit;   // flood danger limit in meters (equivalent to majorFloodLevel)
    private Double majorFloodLevel; // Major flood level (meters)
    private Double minorFloodLevel; // Minor flood level (meters)
    private Double alertWaterLevel; // Alert water level (meters)

    // Status: NORMAL, WARNING, CRITICAL
    private String status;

    // Trend: STABLE, RISING, FALLING
    private String trend;

    // Percentage of maximum
    private double percentageOfMax;

    // Who updated it
    private String updatedBy;       // Irrigation officer ID
    private String updatedByName;

    private String officerId;
    private String officerName;
    private String reportedBy;
    private String reporterName;

    private String officerNotes;

    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    @org.springframework.data.annotation.PersistenceCreator
    public WaterLevel() {
    }

    public WaterLevel(String id, String locationName, String district, double currentLevel, double maximumLimit, String status, String trend, double percentageOfMax, String updatedBy, String updatedByName, String officerNotes, LocalDateTime lastUpdated, LocalDateTime createdAt) {
        this.id = id;
        this.locationName = locationName;
        this.district = district;
        this.currentLevel = currentLevel;
        this.maximumLimit = maximumLimit;
        this.status = status;
        this.trend = trend;
        this.percentageOfMax = percentageOfMax;
        this.updatedBy = updatedBy;
        this.updatedByName = updatedByName;
        this.officerId = updatedBy;
        this.officerName = updatedByName;
        this.reportedBy = updatedBy;
        this.reporterName = updatedByName;
        this.officerNotes = officerNotes;
        this.lastUpdated = lastUpdated;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public double getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(double currentLevel) { this.currentLevel = currentLevel; }
    public double getMaximumLimit() {
        if (majorFloodLevel != null && majorFloodLevel > 0) return majorFloodLevel;
        return maximumLimit;
    }
    public void setMaximumLimit(double maximumLimit) {
        this.maximumLimit = maximumLimit;
        if (this.majorFloodLevel == null || this.majorFloodLevel <= 0) {
            this.majorFloodLevel = maximumLimit;
        }
    }
    public Double getMajorFloodLevel() {
        if (majorFloodLevel != null && majorFloodLevel > 0) return majorFloodLevel;
        return maximumLimit > 0 ? maximumLimit : null;
    }
    public void setMajorFloodLevel(Double majorFloodLevel) {
        this.majorFloodLevel = majorFloodLevel;
        if (majorFloodLevel != null && majorFloodLevel > 0) {
            this.maximumLimit = majorFloodLevel;
        }
    }
    public Double getMinorFloodLevel() { return minorFloodLevel; }
    public void setMinorFloodLevel(Double minorFloodLevel) { this.minorFloodLevel = minorFloodLevel; }
    public Double getAlertWaterLevel() { return alertWaterLevel; }
    public void setAlertWaterLevel(Double alertWaterLevel) { this.alertWaterLevel = alertWaterLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public double getPercentageOfMax() { return percentageOfMax; }
    public void setPercentageOfMax(double percentageOfMax) { this.percentageOfMax = percentageOfMax; }

    public String getUpdatedBy() {
        if (updatedBy != null && !updatedBy.isBlank()) return updatedBy;
        if (officerId != null && !officerId.isBlank()) return officerId;
        return reportedBy;
    }
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        if (this.officerId == null || this.officerId.isBlank()) this.officerId = updatedBy;
        if (this.reportedBy == null || this.reportedBy.isBlank()) this.reportedBy = updatedBy;
    }

    public String getUpdatedByName() {
        if (updatedByName != null && !updatedByName.isBlank()) return updatedByName;
        if (officerName != null && !officerName.isBlank()) return officerName;
        return reporterName;
    }
    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
        if (this.officerName == null || this.officerName.isBlank()) this.officerName = updatedByName;
        if (this.reporterName == null || this.reporterName.isBlank()) this.reporterName = updatedByName;
    }

    public String getOfficerId() {
        if (officerId != null && !officerId.isBlank()) return officerId;
        if (updatedBy != null && !updatedBy.isBlank()) return updatedBy;
        return reportedBy;
    }
    public void setOfficerId(String officerId) {
        this.officerId = officerId;
        if (this.updatedBy == null || this.updatedBy.isBlank()) this.updatedBy = officerId;
        if (this.reportedBy == null || this.reportedBy.isBlank()) this.reportedBy = officerId;
    }

    public String getOfficerName() {
        if (officerName != null && !officerName.isBlank()) return officerName;
        if (updatedByName != null && !updatedByName.isBlank()) return updatedByName;
        return reporterName;
    }
    public void setOfficerName(String officerName) {
        this.officerName = officerName;
        if (this.updatedByName == null || this.updatedByName.isBlank()) this.updatedByName = officerName;
        if (this.reporterName == null || this.reporterName.isBlank()) this.reporterName = officerName;
    }

    public String getReportedBy() {
        if (reportedBy != null && !reportedBy.isBlank()) return reportedBy;
        if (updatedBy != null && !updatedBy.isBlank()) return updatedBy;
        return officerId;
    }
    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
        if (this.updatedBy == null || this.updatedBy.isBlank()) this.updatedBy = reportedBy;
        if (this.officerId == null || this.officerId.isBlank()) this.officerId = reportedBy;
    }

    public String getReporterName() {
        if (reporterName != null && !reporterName.isBlank()) return reporterName;
        if (updatedByName != null && !updatedByName.isBlank()) return updatedByName;
        return officerName;
    }
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
        if (this.updatedByName == null || this.updatedByName.isBlank()) this.updatedByName = reporterName;
        if (this.officerName == null || this.officerName.isBlank()) this.officerName = reporterName;
    }

    public String getOfficerNotes() { return officerNotes; }
    public void setOfficerNotes(String officerNotes) { this.officerNotes = officerNotes; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class WaterLevelReading {
        private double level;
        private LocalDateTime timestamp;
        private String status;
        private String officerName;

        public WaterLevelReading() {}

        public WaterLevelReading(double level, LocalDateTime timestamp, String status, String officerName) {
            this.level = level;
            this.timestamp = timestamp;
            this.status = status;
            this.officerName = officerName;
        }

        public double getLevel() { return level; }
        public void setLevel(double level) { this.level = level; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getOfficerName() { return officerName; }
        public void setOfficerName(String officerName) { this.officerName = officerName; }
    }

    private List<WaterLevelReading> history = new ArrayList<>();

    public List<WaterLevelReading> getHistory() {
        if (history == null) {
            history = new ArrayList<>();
        }
        return history;
    }

    public void setHistory(List<WaterLevelReading> history) {
        this.history = history != null ? history : new ArrayList<>();
    }
}
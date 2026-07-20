package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "water_levels")
public class WaterLevel {

    @Id
    private String id;

    // Location name e.g., "Nilwala River - Matara"
    private String locationName;

    private String district;   // e.g., "Matara"

    private double currentLevel;   // in meters
    private double maximumLimit;   // flood danger limit in meters

    // Status: NORMAL, WARNING, CRITICAL
    private String status;

    // Trend: STABLE, RISING, FALLING
    private String trend;

    // Percentage of maximum
    private double percentageOfMax;

    // Who updated it
    private String updatedBy;       // Irrigation officer ID
    private String updatedByName;

    private String officerNotes;

    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

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
    public double getMaximumLimit() { return maximumLimit; }
    public void setMaximumLimit(double maximumLimit) { this.maximumLimit = maximumLimit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public double getPercentageOfMax() { return percentageOfMax; }
    public void setPercentageOfMax(double percentageOfMax) { this.percentageOfMax = percentageOfMax; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public String getUpdatedByName() { return updatedByName; }
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }
    public String getOfficerNotes() { return officerNotes; }
    public void setOfficerNotes(String officerNotes) { this.officerNotes = officerNotes; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.model.WaterLevel;
import com.edrr.edrr_backend.repository.WaterLevelRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaterLevelService {
    public WaterLevelService(WaterLevelRepository waterLevelRepository) {
        this.waterLevelRepository = waterLevelRepository;
    }


    private final WaterLevelRepository waterLevelRepository;

    // ── ADD NEW WATER LOCATION ───────────────────────────────────────────
    public WaterLevel addLocation(WaterLevel waterLevel) {
        waterLevel.setCreatedAt(LocalDateTime.now());
        waterLevel.setLastUpdated(LocalDateTime.now());
        if (waterLevel.getReporterName() == null || waterLevel.getReporterName().isBlank()) {
            waterLevel.setReporterName("Irrigation Officer");
        }
        if (waterLevel.getMajorFloodLevel() != null && waterLevel.getMajorFloodLevel() > 0) {
            waterLevel.setMaximumLimit(waterLevel.getMajorFloodLevel());
        } else if (waterLevel.getMaximumLimit() > 0) {
            waterLevel.setMajorFloodLevel(waterLevel.getMaximumLimit());
        }
        calculateStatus(waterLevel); // Set initial status
        if (waterLevel.getHistory() == null) {
            waterLevel.setHistory(new java.util.ArrayList<>());
        }
        waterLevel.getHistory().add(new WaterLevel.WaterLevelReading(
                waterLevel.getCurrentLevel(),
                waterLevel.getLastUpdated() != null ? waterLevel.getLastUpdated() : LocalDateTime.now(),
                waterLevel.getStatus(),
                waterLevel.getReporterName()
        ));
        return waterLevelRepository.save(waterLevel);
    }

    // ── GET ALL WATER LEVELS ─────────────────────────────────────────────
    public List<WaterLevel> getAllWaterLevels() {
        return waterLevelRepository.findAll();
    }

    // ── GET BY DISTRICT ──────────────────────────────────────────────────
    public List<WaterLevel> getByDistrict(String district) {
        return waterLevelRepository.findByDistrict(district);
    }

    // ── GET CRITICAL LEVELS (for flood alerts) ───────────────────────────
    public List<WaterLevel> getCriticalLevels() {
        return waterLevelRepository.findByStatus("CRITICAL");
    }

    // ── UPDATE WATER LEVEL ───────────────────────────────────────────────
    public WaterLevel updateLevel(String id, double newLevel, Double newMaxLimit,
                                  String officerId, String officerName, String notes) {
        return updateLevel(id, newLevel, newMaxLimit, null, null, officerId, officerName, notes);
    }

    public WaterLevel updateLevel(String id, double newLevel, Double newMaxLimit,
                                  Double minorLevel, Double alertLevel,
                                  String officerId, String officerName, String notes) {

        WaterLevel waterLevel = waterLevelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water location not found: " + id));

        double previousLevel = waterLevel.getCurrentLevel();

        // Update values
        waterLevel.setCurrentLevel(newLevel);

        if (newMaxLimit != null && newMaxLimit > 0) {
            waterLevel.setMaximumLimit(newMaxLimit);
            waterLevel.setMajorFloodLevel(newMaxLimit);
        }
        if (minorLevel != null && minorLevel > 0) {
            waterLevel.setMinorFloodLevel(minorLevel);
        }
        if (alertLevel != null && alertLevel > 0) {
            waterLevel.setAlertWaterLevel(alertLevel);
        }

        // Determine trend (rising/falling/stable)
        if (newLevel > previousLevel + 0.1) {
            waterLevel.setTrend("RISING");
        } else if (newLevel < previousLevel - 0.1) {
            waterLevel.setTrend("FALLING");
        } else {
            waterLevel.setTrend("STABLE");
        }

        // Calculate percentage and status based on Major, Minor, and Alert levels
        calculateStatus(waterLevel);

        if (officerId != null && !officerId.isBlank()) {
            waterLevel.setUpdatedBy(officerId);
            waterLevel.setOfficerId(officerId);
            waterLevel.setReportedBy(officerId);
        }
        if (officerName != null && !officerName.isBlank()) {
            waterLevel.setUpdatedByName(officerName);
            waterLevel.setOfficerName(officerName);
            waterLevel.setReporterName(officerName);
        }
        if (notes != null) {
            waterLevel.setOfficerNotes(notes);
        }
        LocalDateTime now = LocalDateTime.now();
        waterLevel.setLastUpdated(now);

        // Maintain 1-week reading history (prune older than 7 days)
        if (waterLevel.getHistory() == null) {
            waterLevel.setHistory(new java.util.ArrayList<>());
        }
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        waterLevel.getHistory().removeIf(r -> r.getTimestamp() != null && r.getTimestamp().isBefore(sevenDaysAgo));
        waterLevel.getHistory().add(new WaterLevel.WaterLevelReading(
                newLevel,
                now,
                waterLevel.getStatus(),
                officerName != null && !officerName.isBlank() ? officerName : waterLevel.getReporterName()
        ));

        return waterLevelRepository.save(waterLevel);
    }

    // ── PRIVATE: Calculate Status Based on Major, Minor, and Alert Levels ─
    private void calculateStatus(WaterLevel wl) {
        Double major = wl.getMajorFloodLevel();
        if (major == null && wl.getMaximumLimit() > 0) {
            major = wl.getMaximumLimit();
        }
        Double minor = wl.getMinorFloodLevel();
        Double alert = wl.getAlertWaterLevel();
        double current = wl.getCurrentLevel();

        if (major != null && major > 0) {
            double percentage = (current / major) * 100.0;
            wl.setPercentageOfMax(Math.round(percentage * 10.0) / 10.0);
        }

        // Major Flood Level Alert (RED)
        if (major != null && major > 0 && current >= major) {
            wl.setStatus("CRITICAL");
        }
        // Minor Flood Level Alert (ORANGE)
        else if (minor != null && minor > 0 && current >= minor) {
            wl.setStatus("WARNING");
        }
        // Alert Water Level Alert (YELLOW)
        else if (alert != null && alert > 0 && current >= alert) {
            wl.setStatus("ALERT");
        }
        // Fallback: If minor/alert not set but major is set, use percentage brackets
        else if (major != null && major > 0) {
            double percentage = (current / major) * 100.0;
            if (percentage >= 100) {
                wl.setStatus("CRITICAL");
            } else if (percentage >= 80) {
                wl.setStatus("WARNING");
            } else if (percentage >= 60) {
                wl.setStatus("ALERT");
            } else {
                wl.setStatus("NORMAL");
            }
        } else {
            wl.setStatus("NORMAL");
        }
    }
}
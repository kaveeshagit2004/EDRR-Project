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
        calculateStatus(waterLevel); // Set initial status
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

        WaterLevel waterLevel = waterLevelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Water location not found: " + id));

        double previousLevel = waterLevel.getCurrentLevel();

        // Update values
        waterLevel.setCurrentLevel(newLevel);

        if (newMaxLimit != null) {
            waterLevel.setMaximumLimit(newMaxLimit);
        }

        // Determine trend (rising/falling/stable)
        if (newLevel > previousLevel + 0.1) {
            waterLevel.setTrend("RISING");
        } else if (newLevel < previousLevel - 0.1) {
            waterLevel.setTrend("FALLING");
        } else {
            waterLevel.setTrend("STABLE");
        }

        // Calculate percentage and status
        calculateStatus(waterLevel);

        waterLevel.setUpdatedBy(officerId);
        waterLevel.setUpdatedByName(officerName);
        waterLevel.setOfficerNotes(notes);
        waterLevel.setLastUpdated(LocalDateTime.now());

        return waterLevelRepository.save(waterLevel);
    }

    // ── PRIVATE: Calculate Status Based on Percentage ────────────────────
    private void calculateStatus(WaterLevel wl) {
        if (wl.getMaximumLimit() > 0) {
            double percentage = (wl.getCurrentLevel() / wl.getMaximumLimit()) * 100;
            wl.setPercentageOfMax(Math.round(percentage * 10.0) / 10.0);

            // Set status based on percentage
            if (percentage >= 100) {
                wl.setStatus("CRITICAL");  // Flood danger!
            } else if (percentage >= 80) {
                wl.setStatus("WARNING");   // Getting close
            } else {
                wl.setStatus("NORMAL");    // All good
            }
        }
    }
}
package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.model.WaterLevel;
import com.edrr.edrr_backend.services.WaterLevelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/water-levels")
@CrossOrigin(origins = "*")
public class WaterLevelController {
    public WaterLevelController(WaterLevelService waterLevelService) {
        this.waterLevelService = waterLevelService;
    }


    private final WaterLevelService waterLevelService;

    // ── ADD NEW LOCATION ──────────────────────────────────────────────────
    // POST http://localhost:8080/api/water-levels
    @PostMapping
    public ResponseEntity<?> addLocation(@RequestBody Map<String, Object> body) {
        try {
            WaterLevel wl = new WaterLevel();
            if (body.containsKey("locationName") && body.get("locationName") != null) {
                wl.setLocationName(body.get("locationName").toString());
            }
            if (body.containsKey("district") && body.get("district") != null) {
                wl.setDistrict(body.get("district").toString());
            }
            if (body.containsKey("currentLevel") && body.get("currentLevel") != null && !body.get("currentLevel").toString().isBlank()) {
                wl.setCurrentLevel(Double.parseDouble(body.get("currentLevel").toString()));
            }
            Double maxLimit = body.containsKey("majorFloodLevel") && body.get("majorFloodLevel") != null && !body.get("majorFloodLevel").toString().isBlank() ?
                    Double.parseDouble(body.get("majorFloodLevel").toString()) :
                    body.containsKey("maximumLimit") && body.get("maximumLimit") != null && !body.get("maximumLimit").toString().isBlank() ?
                    Double.parseDouble(body.get("maximumLimit").toString()) : null;
            if (maxLimit != null && maxLimit > 0) {
                wl.setMaximumLimit(maxLimit);
                wl.setMajorFloodLevel(maxLimit);
            }
            if (body.containsKey("minorFloodLevel") && body.get("minorFloodLevel") != null && !body.get("minorFloodLevel").toString().isBlank()) {
                wl.setMinorFloodLevel(Double.parseDouble(body.get("minorFloodLevel").toString()));
            }
            if (body.containsKey("alertWaterLevel") && body.get("alertWaterLevel") != null && !body.get("alertWaterLevel").toString().isBlank()) {
                wl.setAlertWaterLevel(Double.parseDouble(body.get("alertWaterLevel").toString()));
            }
            String officerId = (String) (body.containsKey("officerId") ? body.get("officerId") :
                    body.containsKey("reportedBy") ? body.get("reportedBy") : body.get("updatedBy"));
            String officerName = (String) (body.containsKey("officerName") ? body.get("officerName") :
                    body.containsKey("reporterName") ? body.get("reporterName") : body.get("updatedByName"));
            String notes = (String) body.get("notes");
            wl.setOfficerId(officerId);
            wl.setReportedBy(officerId);
            wl.setUpdatedBy(officerId);
            wl.setOfficerName(officerName);
            wl.setReporterName(officerName);
            wl.setUpdatedByName(officerName);
            wl.setOfficerNotes(notes);

            return ResponseEntity.ok(waterLevelService.addLocation(wl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET ALL WATER LEVELS ──────────────────────────────────────────────
    // GET http://localhost:8080/api/water-levels
    @GetMapping
    public ResponseEntity<List<WaterLevel>> getAll() {
        return ResponseEntity.ok(waterLevelService.getAllWaterLevels());
    }

    // ── GET BY DISTRICT ───────────────────────────────────────────────────
    // GET http://localhost:8080/api/water-levels/district/Matara
    @GetMapping("/district/{district}")
    public ResponseEntity<List<WaterLevel>> getByDistrict(@PathVariable String district) {
        return ResponseEntity.ok(waterLevelService.getByDistrict(district));
    }

    // ── GET CRITICAL LEVELS ───────────────────────────────────────────────
    // GET http://localhost:8080/api/water-levels/critical
    @GetMapping("/critical")
    public ResponseEntity<List<WaterLevel>> getCritical() {
        return ResponseEntity.ok(waterLevelService.getCriticalLevels());
    }

    // ── UPDATE WATER LEVEL ────────────────────────────────────────────────
    // PUT http://localhost:8080/api/water-levels/{id}
    // Body: { "currentLevel": 3.5, "maximumLimit": 4.0,
    //         "officerId": "xxx", "officerName": "John", "notes": "Rising fast" }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLevel(@PathVariable String id,
                                         @RequestBody Map<String, Object> body) {
        try {
            double level = Double.parseDouble(body.get("currentLevel").toString());
            Double maxLimit = body.containsKey("majorFloodLevel") && body.get("majorFloodLevel") != null && !body.get("majorFloodLevel").toString().isBlank() ?
                    Double.parseDouble(body.get("majorFloodLevel").toString()) :
                    body.containsKey("maximumLimit") && body.get("maximumLimit") != null && !body.get("maximumLimit").toString().isBlank() ?
                    Double.parseDouble(body.get("maximumLimit").toString()) : null;
            Double minorLevel = body.containsKey("minorFloodLevel") && body.get("minorFloodLevel") != null && !body.get("minorFloodLevel").toString().isBlank() ?
                    Double.parseDouble(body.get("minorFloodLevel").toString()) : null;
            Double alertLevel = body.containsKey("alertWaterLevel") && body.get("alertWaterLevel") != null && !body.get("alertWaterLevel").toString().isBlank() ?
                    Double.parseDouble(body.get("alertWaterLevel").toString()) : null;
            String officerId = (String) (body.containsKey("officerId") ? body.get("officerId") :
                    body.containsKey("reportedBy") ? body.get("reportedBy") : body.get("updatedBy"));
            String officerName = (String) (body.containsKey("officerName") ? body.get("officerName") :
                    body.containsKey("reporterName") ? body.get("reporterName") : body.get("updatedByName"));
            String notes = (String) body.get("notes");

            return ResponseEntity.ok(
                    waterLevelService.updateLevel(id, level, maxLimit, minorLevel, alertLevel, officerId, officerName, notes)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
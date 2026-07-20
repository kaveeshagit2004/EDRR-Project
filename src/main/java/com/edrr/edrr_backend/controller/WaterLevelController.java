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
    public ResponseEntity<?> addLocation(@RequestBody WaterLevel waterLevel) {
        try {
            return ResponseEntity.ok(waterLevelService.addLocation(waterLevel));
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
            Double maxLimit = body.containsKey("maximumLimit") ?
                    Double.parseDouble(body.get("maximumLimit").toString()) : null;
            String officerId = (String) body.get("officerId");
            String officerName = (String) body.get("officerName");
            String notes = (String) body.get("notes");

            return ResponseEntity.ok(
                    waterLevelService.updateLevel(id, level, maxLimit, officerId, officerName, notes)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.dto.PublicDamageReportDto;
import com.edrr.edrr_backend.model.PublicDamageReport;
import com.edrr.edrr_backend.model.SystemSetting;
import com.edrr.edrr_backend.services.PublicDamageReportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public-reports")
@CrossOrigin(origins = "*")
public class PublicDamageReportController {

    private final PublicDamageReportService service;

    public PublicDamageReportController(PublicDamageReportService service) {
        this.service = service;
    }

    // ── CREATE PUBLIC REPORT (CITIZEN SUBMISSION) ─────────────────────────
    // POST http://localhost:8080/api/public-reports
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReport(@Valid @ModelAttribute PublicDamageReportDto dto) {
        try {
            PublicDamageReport saved = service.createReport(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET ALL PUBLIC REPORTS ───────────────────────────────────────────
    // GET http://localhost:8080/api/public-reports
    @GetMapping
    public ResponseEntity<List<PublicDamageReport>> getAllReports() {
        return ResponseEntity.ok(service.getAllReports());
    }

    // ── GET PUBLIC REPORT BY ID ──────────────────────────────────────────
    // GET http://localhost:8080/api/public-reports/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.getReportById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── UPDATE STATUS & NOTES ────────────────────────────────────────────
    // PUT http://localhost:8080/api/public-reports/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            String adminNotes = body.get("adminNotes");
            return ResponseEntity.ok(service.updateStatus(id, status, adminNotes));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── UPDATE RECOVERY PROGRESS ─────────────────────────────────────────
    // PUT http://localhost:8080/api/public-reports/{id}/progress
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        try {
            int progress = body.getOrDefault("progress", 0);
            return ResponseEntity.ok(service.updateProgress(id, progress));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── SOFT DELETE PUBLIC REPORT ────────────────────────────────────────
    // DELETE http://localhost:8080/api/public-reports/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(
            @PathVariable String id,
            @RequestParam(value = "userEmail", required = false) String paramEmail,
            Principal principal) {
        try {
            String userEmail = (principal != null && principal.getName() != null && !principal.getName().isBlank())
                    ? principal.getName()
                    : paramEmail;
            service.softDeleteReport(id, userEmail);
            return ResponseEntity.ok(Map.of("message", "Report moved to recycle bin successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── RESTORE REPORT ───────────────────────────────────────────────────
    // PUT http://localhost:8080/api/public-reports/{id}/restore
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreReport(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.restoreReport(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── PERMANENTLY DELETE REPORT ────────────────────────────────────────
    // DELETE http://localhost:8080/api/public-reports/{id}/permanent
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentlyDeleteReport(@PathVariable String id) {
        try {
            service.permanentlyDeleteReport(id);
            return ResponseEntity.ok(Map.of("message", "Report permanently deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET PUBLIC REPORTS STATS ─────────────────────────────────────────
    // GET http://localhost:8080/api/public-reports/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    // ── MARK REPORT AS VIEWED (ONLY RDA ADMIN) ───────────────────────────
    // PUT http://localhost:8080/api/public-reports/{id}/view
    @PutMapping("/{id}/view")
    public ResponseEntity<?> markAsViewed(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body,
            Principal principal) {
        try {
            boolean viewed = true;
            if (body != null && body.containsKey("viewed")) {
                viewed = Boolean.parseBoolean(String.valueOf(body.get("viewed")));
            }
            String userEmail = principal != null && principal.getName() != null && !principal.getName().isBlank()
                    ? principal.getName()
                    : (body != null && body.containsKey("userEmail") ? String.valueOf(body.get("userEmail")) : "rivi@gmail.com");
            PublicDamageReport updated = service.markAsViewedByRda(id, userEmail, viewed);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET CITIZEN REPORTING STATUS (PUBLIC ACCESS) ─────────────────────
    // GET http://localhost:8080/api/public-reports/reporting-status
    @GetMapping("/reporting-status")
    public ResponseEntity<Map<String, Object>> getReportingStatus() {
        SystemSetting setting = service.getDamageReportingSetting();
        Map<String, Object> res = new HashMap<>();
        res.put("open", service.isDamageReportingOpen());
        res.put("updatedAt", setting.getUpdatedAt());
        res.put("updatedBy", setting.getUpdatedBy());
        return ResponseEntity.ok(res);
    }

    // ── TOGGLE CITIZEN REPORTING STATUS (GA ADMIN ONLY) ──────────────────
    // PUT http://localhost:8080/api/public-reports/reporting-status
    @PutMapping("/reporting-status")
    public ResponseEntity<?> updateReportingStatus(
            @RequestBody Map<String, Object> body,
            Principal principal) {
        try {
            boolean open = false;
            if (body != null && body.containsKey("open")) {
                open = Boolean.parseBoolean(String.valueOf(body.get("open")));
            }
            String userEmail = principal != null && principal.getName() != null && !principal.getName().isBlank()
                    ? principal.getName()
                    : (body != null && body.containsKey("userEmail") ? String.valueOf(body.get("userEmail")) : "kaveesha@gmail.com");
            SystemSetting setting = service.setDamageReportingOpen(open, userEmail);
            Map<String, Object> res = new HashMap<>();
            res.put("open", Boolean.parseBoolean(setting.getValue()));
            res.put("updatedAt", setting.getUpdatedAt());
            res.put("updatedBy", setting.getUpdatedBy());
            res.put("message", "Citizen damage reporting is now " + (open ? "OPEN" : "CLOSED"));
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.dto.DamageReportDto;
import com.edrr.edrr_backend.model.DamageReport;
import com.edrr.edrr_backend.services.DamageReportService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class DamageReportController {
    public DamageReportController(DamageReportService reportService) {
        this.reportService = reportService;
    }


    private final DamageReportService reportService;

    // ── CREATE REPORT ─────────────────────────────────────────────────────
    // POST http://localhost:8080/api/reports
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReport(@Valid @ModelAttribute DamageReportDto report) {
        try {
            DamageReport saved = reportService.createReport(report);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET ALL REPORTS ───────────────────────────────────────────────────
    // GET http://localhost:8080/api/reports
    @GetMapping
    public ResponseEntity<List<DamageReport>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    // ── SERVE PHOTO ──────────────────────────────────────────────────────
    // GET http://localhost:8080/api/reports/photos/{filename}
    @GetMapping("/photos/{filename:.+}")
    public ResponseEntity<Resource> servePhoto(@PathVariable String filename, jakarta.servlet.http.HttpServletRequest request) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(System.getProperty("user.dir") + "/stored_photos/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            // Fallback 1: Try nested edrr-backend directory
            if (!resource.exists() || !resource.isReadable()) {
                java.nio.file.Path alternativePath = java.nio.file.Paths.get(System.getProperty("user.dir") + "/edrr-backend/stored_photos/").resolve(filename).normalize();
                resource = new UrlResource(alternativePath.toUri());
            }
            
            // Fallback 2: Try parent directory (if server is running inside the inner folder)
            if (!resource.exists() || !resource.isReadable()) {
                java.nio.file.Path alternativePath = java.nio.file.Paths.get(System.getProperty("user.dir") + "/../stored_photos/").resolve(filename).normalize();
                resource = new UrlResource(alternativePath.toUri());
            }

            if (resource.exists() && resource.isReadable()) {
                String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── GET REPORT BY ID ──────────────────────────────────────────────────
    // GET http://localhost:8080/api/reports/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(reportService.getReportById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── GET REPORTS BY STATUS ─────────────────────────────────────────────
    // GET http://localhost:8080/api/reports/status/PENDING
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DamageReport>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status.toUpperCase()));
    }

    // ── GET REPORTS BY OFFICER ────────────────────────────────────────────
    // GET http://localhost:8080/api/reports/officer/{officerId}
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<DamageReport>> getByOfficer(@PathVariable String officerId) {
        return ResponseEntity.ok(reportService.getReportsByOfficer(officerId));
    }



    // ── UPDATE STATUS ─────────────────────────────────────────────────────
    // PUT http://localhost:8080/api/reports/{id}/status
    // Body: { "status": "IN_PROGRESS", "adminNotes": "Team dispatched" }
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id,
                                          @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            String notes = body.get("adminNotes");
            return ResponseEntity.ok(reportService.updateStatus(id, status, notes));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── UPDATE RECOVERY PROGRESS ──────────────────────────────────────────
    // PUT http://localhost:8080/api/reports/{id}/progress
    // Body: { "progress": 75 }
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable String id,
                                            @RequestBody Map<String, Integer> body) {
        try {
            int progress = body.get("progress");
            return ResponseEntity.ok(reportService.updateProgress(id, progress));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DASHBOARD STATS ───────────────────────────────────────────────────
    // GET http://localhost:8080/api/reports/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    // ── DELETE REPORT ─────────────────────────────────────────────────────
    // DELETE http://localhost:8080/api/reports/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable String id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
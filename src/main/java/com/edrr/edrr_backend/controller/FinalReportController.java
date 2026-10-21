package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.model.FinalReport;
import com.edrr.edrr_backend.services.FinalReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/final-reports")
@CrossOrigin(origins = "*")
public class FinalReportController {

    private final FinalReportService finalReportService;

    public FinalReportController(FinalReportService finalReportService) {
        this.finalReportService = finalReportService;
    }

    // ── UPLOAD FINAL REPORT ──────────────────────────────────────────────
    // POST http://localhost:8080/api/final-reports
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFinalReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("damageType") String damageType,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy,
            @RequestParam(value = "uploadedByRole", required = false) String uploadedByRole
    ) {
        try {
            FinalReport report = finalReportService.saveFinalReport(file, damageType, notes, uploadedBy, uploadedByRole);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload final report: " + e.getMessage()));
        }
    }

    // ── GET ALL FINAL REPORTS ────────────────────────────────────────────
    // GET http://localhost:8080/api/final-reports
    @GetMapping
    public ResponseEntity<List<FinalReport>> getAllFinalReports() {
        return ResponseEntity.ok(finalReportService.getAllFinalReports());
    }

    // ── DOWNLOAD FINAL REPORT BY ID ──────────────────────────────────────
    // GET http://localhost:8080/api/final-reports/download/{id}
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFinalReport(@PathVariable String id, HttpServletRequest request) {
        try {
            FinalReport report = finalReportService.getFinalReportById(id);
            Resource resource = finalReportService.loadFileAsResource(report.getStoredFileName());

            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null || contentType.isBlank()) {
                contentType = report.getFileType() != null ? report.getFileType() : "application/octet-stream";
            }

            String encodedFileName = URLEncoder.encode(report.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFileName() + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── SERVE FILE DIRECTLY ──────────────────────────────────────────────
    // GET http://localhost:8080/api/final-reports/files/{filename}
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFinalReportFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            Resource resource = finalReportService.loadFileAsResource(filename);
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── SOFT DELETE FINAL REPORT (MOVE TO RECYCLE BIN WITH REASON) ───────
    // DELETE http://localhost:8080/api/final-reports/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFinalReport(
            @PathVariable String id,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "deletedBy", required = false) String deletedBy,
            @RequestParam(value = "deletedByName", required = false) String deletedByName
    ) {
        try {
            FinalReport report = finalReportService.softDeleteFinalReport(id, deletedBy, deletedByName, reason);
            return ResponseEntity.ok(Map.of("message", "Final report moved to recycle bin", "report", report));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── GET DELETED FINAL REPORTS (RECYCLE BIN) ──────────────────────────
    // GET http://localhost:8080/api/final-reports/recycle-bin
    @GetMapping("/recycle-bin")
    public ResponseEntity<List<FinalReport>> getDeletedFinalReports() {
        return ResponseEntity.ok(finalReportService.getDeletedFinalReports());
    }

    // ── GET RDA/DMC SPECIFIC NOTIFICATIONS ──────────────────────────────
    // GET http://localhost:8080/api/final-reports/rda-notifications
    @GetMapping("/rda-notifications")
    public ResponseEntity<List<FinalReport>> getRdaNotifications() {
        return ResponseEntity.ok(finalReportService.getRdaNotifications());
    }

    // ── RESTORE FINAL REPORT FROM RECYCLE BIN ────────────────────────────
    // PUT http://localhost:8080/api/final-reports/{id}/restore
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreFinalReport(@PathVariable String id) {
        try {
            FinalReport report = finalReportService.restoreFinalReport(id);
            return ResponseEntity.ok(Map.of("message", "Final report restored successfully", "report", report));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── PERMANENTLY DELETE FINAL REPORT ──────────────────────────────────
    // DELETE http://localhost:8080/api/final-reports/{id}/permanent
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentlyDeleteFinalReport(@PathVariable String id) {
        try {
            finalReportService.permanentlyDeleteFinalReport(id);
            return ResponseEntity.ok(Map.of("message", "Final report permanently deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

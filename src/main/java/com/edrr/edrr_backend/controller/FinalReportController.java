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

    // ── DELETE FINAL REPORT ──────────────────────────────────────────────
    // DELETE http://localhost:8080/api/final-reports/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFinalReport(@PathVariable String id) {
        try {
            finalReportService.deleteFinalReport(id);
            return ResponseEntity.ok(Map.of("message", "Final report deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

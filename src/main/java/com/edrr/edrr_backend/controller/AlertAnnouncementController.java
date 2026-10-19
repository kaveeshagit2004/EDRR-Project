package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.model.AlertAnnouncement;
import com.edrr.edrr_backend.services.AlertAnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AlertAnnouncementController {

    private final AlertAnnouncementService announcementService;

    public AlertAnnouncementController(AlertAnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    // ── SEND ALERT ANNOUNCEMENT (GA ADMIN ONLY) ──────────────────────────
    // POST http://localhost:8080/api/announcements
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendAnnouncement(
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam(value = "alertLevel", required = false, defaultValue = "WARNING") String alertLevel,
            @RequestParam(value = "targetAudience", required = false, defaultValue = "ALL") String targetAudience,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "senderName", required = false) String senderName,
            @RequestParam(value = "senderEmail", required = false) String senderEmail,
            @RequestParam(value = "senderRole", required = false) String senderRole,
            Authentication authentication
    ) {
        try {
            String effectiveRole = senderRole;
            String effectiveEmail = senderEmail;

            if (authentication != null && authentication.isAuthenticated()) {
                effectiveEmail = authentication.getName();
                boolean isGa = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().contains("GA_ADMIN"));
                if (isGa) {
                    effectiveRole = "GA_ADMIN";
                }
            }

            // Verify GA Admin authorization
            if (effectiveRole != null && !effectiveRole.equalsIgnoreCase("GA_ADMIN") && !effectiveRole.equalsIgnoreCase("ROLE_GA_ADMIN")) {
                return ResponseEntity.status(403).body(Map.of("error", "Only GA Admin is authorized to send alert announcements"));
            }

            AlertAnnouncement announcement = announcementService.createAnnouncement(
                    title, message, alertLevel, targetAudience, image, effectiveEmail, senderName
            );
            return ResponseEntity.ok(announcement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send announcement: " + e.getMessage()));
        }
    }

    // ── GET ALL ANNOUNCEMENTS ────────────────────────────────────────────
    // GET http://localhost:8080/api/announcements
    @GetMapping
    public ResponseEntity<List<AlertAnnouncement>> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    // ── GET ACTIVE ANNOUNCEMENTS ─────────────────────────────────────────
    // GET http://localhost:8080/api/announcements/active
    @GetMapping("/active")
    public ResponseEntity<List<AlertAnnouncement>> getActiveAnnouncements() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }

    // ── DELETE ANNOUNCEMENT (GA ADMIN ONLY) ───────────────────────────────
    // DELETE http://localhost:8080/api/announcements/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(
            @PathVariable String id,
            @RequestParam(value = "userRole", required = false, defaultValue = "GA_ADMIN") String userRole,
            Authentication authentication
    ) {
        try {
            String effectiveRole = userRole;
            if (authentication != null && authentication.isAuthenticated()) {
                boolean isGa = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().contains("GA_ADMIN"));
                if (isGa) {
                    effectiveRole = "GA_ADMIN";
                }
            }

            announcementService.deleteAnnouncement(id, effectiveRole);
            return ResponseEntity.ok(Map.of("message", "Alert announcement deleted successfully", "id", id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete announcement: " + e.getMessage()));
        }
    }

    // ── SERVE ANNOUNCEMENT IMAGE ─────────────────────────────────────────
    // GET http://localhost:8080/api/announcements/images/{filename}
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename, HttpServletRequest request) {
        try {
            Resource resource = announcementService.loadImageAsResource(filename);
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ignored) {}

            if (contentType == null) {
                if (filename.toLowerCase().endsWith(".png")) contentType = "image/png";
                else if (filename.toLowerCase().endsWith(".gif")) contentType = "image/gif";
                else if (filename.toLowerCase().endsWith(".webp")) contentType = "image/webp";
                else contentType = "image/jpeg";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

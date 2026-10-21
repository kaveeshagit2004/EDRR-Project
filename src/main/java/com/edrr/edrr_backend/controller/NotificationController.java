package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.model.Notification;
import com.edrr.edrr_backend.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ── GET DASHBOARD NOTIFICATIONS (RURAL OFFICERS & RDA/DMC ONLY) ──────
    // GET http://localhost:8080/api/notifications/dashboard?role=...&divisionName=...&userId=...
    @GetMapping("/dashboard")
    public ResponseEntity<List<Notification>> getDashboardNotifications(
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "divisionName", required = false) String divisionName,
            @RequestParam(value = "userId", required = false) String userId
    ) {
        List<Notification> list = notificationService.getOfficerNotifications(role, divisionName, userId);
        return ResponseEntity.ok(list);
    }

    // ── MARK NOTIFICATION AS READ ────────────────────────────────────────
    // PUT http://localhost:8080/api/notifications/{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable String id,
            @RequestParam(value = "userId", required = false) String userId
    ) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    // ── DISMISS NOTIFICATION ──────────────────────────────────────────────
    // PUT http://localhost:8080/api/notifications/{id}/dismiss
    @PutMapping("/{id}/dismiss")
    public ResponseEntity<?> dismissNotification(@PathVariable String id) {
        notificationService.dismissNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification dismissed"));
    }
}

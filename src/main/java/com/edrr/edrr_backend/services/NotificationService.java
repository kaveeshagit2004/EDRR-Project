package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.model.Notification;
import com.edrr.edrr_backend.model.PublicDamageReport;
import com.edrr.edrr_backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ── CREATE NOTIFICATION WHEN CITIZEN UPLOADS REPORT ────────────────────
    public Notification notifyCitizenReportCreated(PublicDamageReport report) {
        Notification notification = new Notification();
        notification.setTitle("New Citizen Report: " + (report.getTitle() != null ? report.getTitle() : "Damage Incident"));

        String div = (report.getDivisionName() != null && !report.getDivisionName().isBlank())
                ? report.getDivisionName().trim()
                : "";
        String loc = (report.getLocation() != null && !report.getLocation().isBlank())
                ? report.getLocation().trim()
                : "";
        String rep = (report.getReporterName() != null && !report.getReporterName().isBlank())
                ? report.getReporterName().trim()
                : "Citizen";

        String message = String.format("New citizen damage report submitted in %s (%s) by %s: \"%s\".",
                div.isEmpty() ? loc : div, loc, rep, report.getTitle() != null ? report.getTitle() : "");

        notification.setMessage(message);
        notification.setType("CITIZEN_REPORT");
        notification.setPriority("HIGH");
        // ONLY rural division officers and RDA/DMC admins are targets
        notification.setTargetRoles(List.of("RURAL_OFFICER", "RDA_ADMIN"));
        notification.setTargetDivision(div);
        notification.setRelatedReportId(report.getId());
        notification.setReportNumber(report.getReportNumber());
        notification.setLocation(loc);
        notification.setReporterName(rep);
        notification.setActive(true);
        notification.setCreatedBy("Citizen Portal");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadByUsers(new ArrayList<>());

        return notificationRepository.save(notification);
    }

    // ── GET NOTIFICATIONS FOR LOGGED IN OFFICER ───────────────────────────
    // Scoped strictly so only relevant Rural Officers and RDA/DMC Officers receive it
    public List<Notification> getOfficerNotifications(String role, String divisionName, String userId) {
        if (role == null) return List.of();

        // STRICT ACCESS CONTROL: Only RURAL_OFFICER and RDA_ADMIN can receive these notifications
        if (!"RURAL_OFFICER".equalsIgnoreCase(role) && !"RDA_ADMIN".equalsIgnoreCase(role)) {
            return List.of();
        }

        List<Notification> active = notificationRepository.findByActiveTrueOrderByCreatedAtDesc();

        return active.stream()
                .filter(n -> {
                    if (n.getTargetRoles() == null) return false;
                    if (!"CITIZEN_REPORT".equalsIgnoreCase(n.getType())) return false;
                    boolean matchesRole = n.getTargetRoles().stream().anyMatch(role::equalsIgnoreCase);
                    if (!matchesRole) return false;

                    // If rural officer, MUST match their specific assigned division
                    if ("RURAL_OFFICER".equalsIgnoreCase(role)) {
                        if (n.getTargetDivision() == null || n.getTargetDivision().isBlank()) {
                            return true; // Unassigned division fallback
                        }
                        if (divisionName == null || divisionName.isBlank()) {
                            return false; // Officer without division cannot view division-targeted reports
                        }
                        return n.getTargetDivision().trim().equalsIgnoreCase(divisionName.trim());
                    }

                    // RDA_ADMIN receives all citizen reports across all divisions
                    return true;
                })
                .limit(20)
                .collect(Collectors.toList());
    }

    // ── MARK NOTIFICATION AS READ BY USER ─────────────────────────────────
    public void markAsRead(String id, String userId) {
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getReadByUsers() == null) {
                n.setReadByUsers(new ArrayList<>());
            }
            if (userId != null && !userId.isBlank() && !n.getReadByUsers().contains(userId)) {
                n.getReadByUsers().add(userId);
                notificationRepository.save(n);
            }
        });
    }

    // ── DISMISS NOTIFICATION PERMANENTLY ──────────────────────────────────
    public void dismissNotification(String id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setActive(false);
            notificationRepository.save(n);
        });
    }
}

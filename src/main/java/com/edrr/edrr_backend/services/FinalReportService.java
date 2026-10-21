package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.model.AlertAnnouncement;
import com.edrr.edrr_backend.model.FinalReport;
import com.edrr.edrr_backend.model.Notification;
import com.edrr.edrr_backend.repository.AlertAnnouncementRepository;
import com.edrr.edrr_backend.repository.FinalReportRepository;
import com.edrr.edrr_backend.repository.NotificationRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FinalReportService {

    private final FinalReportRepository finalReportRepository;
    private final AlertAnnouncementRepository alertAnnouncementRepository;
    private final NotificationRepository notificationRepository;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/stored_final_reports/";

    public FinalReportService(
            FinalReportRepository finalReportRepository,
            AlertAnnouncementRepository alertAnnouncementRepository,
            NotificationRepository notificationRepository
    ) {
        this.finalReportRepository = finalReportRepository;
        this.alertAnnouncementRepository = alertAnnouncementRepository;
        this.notificationRepository = notificationRepository;
        ensureDirectoryExists(UPLOAD_DIR);
    }

    private void ensureDirectoryExists(String dirPath) {
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println("Could not create directory: " + dirPath);
        }
    }

    public FinalReport saveFinalReport(MultipartFile file, String damageType, String notes, String uploadedBy, String uploadedByRole) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Final report file cannot be empty");
        }

        ensureDirectoryExists(UPLOAD_DIR);

        String originalName = file.getOriginalFilename();
        String cleanOriginalName = originalName != null ? Paths.get(originalName).getFileName().toString() : "final_report.pdf";
        String uniqueStoredName = "final_report_" + UUID.randomUUID().toString() + "_" + cleanOriginalName.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path targetPath = Paths.get(UPLOAD_DIR).resolve(uniqueStoredName).normalize();
        Files.copy(file.getInputStream(), targetPath);

        FinalReport report = new FinalReport();
        report.setFileName(cleanOriginalName);
        report.setStoredFileName(uniqueStoredName);
        report.setFileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        report.setFileSize(file.getSize());
        report.setDamageType(damageType != null && !damageType.isBlank() ? damageType.trim().toUpperCase() : "OTHER");
        report.setNotes(notes != null ? notes.trim() : "");
        report.setUploadedBy(uploadedBy != null && !uploadedBy.isBlank() ? uploadedBy.trim() : "RDA Admin");
        report.setUploadedByRole(uploadedByRole != null && !uploadedByRole.isBlank() ? uploadedByRole.trim() : "RDA_ADMIN");
        report.setUploadedAt(LocalDateTime.now());

        FinalReport saved = finalReportRepository.save(report);
        saved.setDownloadUrl("/api/final-reports/download/" + saved.getId());
        return finalReportRepository.save(saved);
    }

    public List<FinalReport> getAllFinalReports() {
        return finalReportRepository.findAllByOrderByUploadedAtDesc()
                .stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    public List<FinalReport> getDeletedFinalReports() {
        return finalReportRepository.findAll().stream()
                .filter(FinalReport::isDeleted)
                .sorted((a, b) -> {
                    if (a.getDeletedAt() == null && b.getDeletedAt() == null) return 0;
                    if (a.getDeletedAt() == null) return 1;
                    if (b.getDeletedAt() == null) return -1;
                    return b.getDeletedAt().compareTo(a.getDeletedAt());
                })
                .toList();
    }

    public FinalReport getFinalReportById(String id) {
        return finalReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Final report not found with id: " + id));
    }

    public Resource loadFileAsResource(String storedFileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // Fallback 1: Nested folder
            if (!resource.exists() || !resource.isReadable()) {
                Path altPath = Paths.get(System.getProperty("user.dir") + "/edrr-backend/stored_final_reports/").resolve(storedFileName).normalize();
                resource = new UrlResource(altPath.toUri());
            }

            // Fallback 2: Parent folder
            if (!resource.exists() || !resource.isReadable()) {
                Path altPath = Paths.get(System.getProperty("user.dir") + "/../stored_final_reports/").resolve(storedFileName).normalize();
                resource = new UrlResource(altPath.toUri());
            }

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + storedFileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + storedFileName, e);
        }
    }

    public FinalReport softDeleteFinalReport(String id, String deletedBy, String deletedByName, String reason) {
        FinalReport report = getFinalReportById(id);
        report.setDeleted(true);
        report.setDeletedAt(LocalDateTime.now());
        report.setDeletedBy(deletedBy != null && !deletedBy.isBlank() ? deletedBy : "ga@edrr.com");
        report.setDeletedByName(deletedByName != null && !deletedByName.isBlank() ? deletedByName : "Government Agent (GA)");
        report.setDeleteReason(reason != null ? reason.trim() : "");
        FinalReport saved = finalReportRepository.save(report);

        // Send notification specifically targeted to RDA/DMC officers (NOT to all officers)
        if (reason != null && !reason.trim().isEmpty()) {
            try {
                String pushMessage = "GA admin remove your uploaded final report on this \"" + reason.trim() + "\". check the report and submit it again";

                Notification notification = new Notification();
                notification.setTitle("Final Report Removed by GA Admin");
                notification.setMessage(pushMessage);
                notification.setType("FINAL_REPORT_DELETED");
                notification.setPriority("HIGH");
                // STRICTLY restricted to RDA / DMC officers only - other officers will NEVER see this
                notification.setTargetRoles(List.of("RDA_ADMIN", "DMC_OFFICER"));
                notification.setRelatedReportId(report.getId());
                notification.setActive(true);
                notification.setCreatedBy(deletedByName != null ? deletedByName : "Government Agent (GA)");
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
            } catch (Exception e) {
                System.err.println("Could not create notification for deleted final report: " + e.getMessage());
            }
        }

        return saved;
    }

    public List<FinalReport> getRdaNotifications() {
        return finalReportRepository.findAll().stream()
                .filter(r -> r.isDeleted() && r.getDeleteReason() != null && !r.getDeleteReason().isBlank())
                .sorted((a, b) -> {
                    if (a.getDeletedAt() == null && b.getDeletedAt() == null) return 0;
                    if (a.getDeletedAt() == null) return 1;
                    if (b.getDeletedAt() == null) return -1;
                    return b.getDeletedAt().compareTo(a.getDeletedAt());
                })
                .limit(5)
                .toList();
    }

    public FinalReport restoreFinalReport(String id) {
        FinalReport report = getFinalReportById(id);
        report.setDeleted(false);
        report.setDeletedAt(null);
        report.setDeletedBy(null);
        report.setDeletedByName(null);
        report.setDeleteReason(null);
        return finalReportRepository.save(report);
    }

    public void permanentlyDeleteFinalReport(String id) {
        FinalReport report = getFinalReportById(id);
        if (report.getStoredFileName() != null) {
            try {
                Path filePath = Paths.get(UPLOAD_DIR).resolve(report.getStoredFileName()).normalize();
                Files.deleteIfExists(filePath);
            } catch (Exception ignored) {
            }
        }
        finalReportRepository.deleteById(id);
    }

    public void deleteFinalReport(String id) {
        softDeleteFinalReport(id, null, null, "Deleted by Administrator");
    }
}

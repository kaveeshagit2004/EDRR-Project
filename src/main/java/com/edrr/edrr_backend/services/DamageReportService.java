package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.dto.DamageReportDto;
import com.edrr.edrr_backend.model.DamageReport;
import com.edrr.edrr_backend.repository.DamageReportRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.edrr.edrr_backend.repository.UserRepository;
import java.util.UUID;

@Service
public class DamageReportService {
    private final DamageReportRepository reportRepository;
    private final UserRepository userRepository;

    public DamageReportService(DamageReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    // Target directory to save files on your host machine
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/stored_photos/";

    // ── CREATE NEW DAMAGE REPORT ─────────────────────────────────────────
    public DamageReport createReport(@Valid DamageReportDto report) {

        // 1. Ensure target directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }


        // Auto-generate report number: RPT-2024-0001
        String reportNumber = generateReportNumber();

        List<String> photoUrls = new ArrayList<>();

        // Save files and get links
        for (MultipartFile file : report.getPhotos()) {
            if (file.isEmpty()) continue;

            // 2. Generate a unique, safe filename using UUID to prevent collisions
            String cleanName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String uniqueFileName = reportNumber + "_" + UUID.randomUUID().toString() + "_" + cleanName;
            String filePath = UPLOAD_DIR + uniqueFileName;

            // 3. Save the binary stream to disk
            File dest = new File(filePath);
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 4. Store the filename in the database (links will be generated when fetching)
            photoUrls.add(uniqueFileName);
        }

        DamageReport dr = new DamageReport();
        dr.setReportNumber(reportNumber);
        dr.setTitle(report.getTitle());
        dr.setDamageType(report.getDamageType());
        dr.setLocation(report.getLocation());
        dr.setLongitude(report.getLongitude());
        dr.setLatitude(report.getLatitude());
        dr.setSeverity(report.getSeverity());
        dr.setDescription(report.getDescription());
        dr.setAffectedArea(report.getAffectedArea());
        dr.setStatus("PENDING");
        dr.setRecoveryProgress(0);
        dr.setReportedBy(report.getReportedBy());
        String repName = report.getReporterName();
        if (repName == null || repName.isBlank()) {
            repName = "Field Officer";
        }
        dr.setReporterName(repName);
        dr.setReportedAt(LocalDateTime.now());
        dr.setPhotoUrls(photoUrls);

        DamageReport saved = reportRepository.save(dr);
        populatePhotoUrls(saved);
        return saved;
    }

    // ── GET ALL REPORTS (ACTIVE ONLY) ────────────────────────────────────
    public List<DamageReport> getAllReports() {
        List<DamageReport> reports = reportRepository.findAllActive();
        reports.forEach(this::populatePhotoUrls);
        return reports;
    }

    // ── GET REPORT BY ID ─────────────────────────────────────────────────
    public DamageReport getReportById(String id) {
        DamageReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
        populatePhotoUrls(report);
        return report;
    }

    // ── GET REPORTS BY STATUS (ACTIVE ONLY) ──────────────────────────────
    public List<DamageReport> getReportsByStatus(String status) {
        List<DamageReport> reports = reportRepository.findByStatusAndNotDeleted(status);
        reports.forEach(this::populatePhotoUrls);
        return reports;
    }

    // ── GET REPORTS BY OFFICER (ACTIVE ONLY) ─────────────────────────────
    public List<DamageReport> getReportsByOfficer(String officerId) {
        List<DamageReport> reports = reportRepository.findByReportedByAndNotDeleted(officerId);
        reports.forEach(this::populatePhotoUrls);
        return reports;
    }

    // ── GET DELETED REPORTS (RECYCLE BIN) ─────────────────────────────────
    public List<DamageReport> getDeletedReports() {
        List<DamageReport> reports = reportRepository.findByDeletedTrue();
        reports.forEach(this::populatePhotoUrls);
        return reports;
    }

    // ── UPDATE REPORT STATUS ─────────────────────────────────────────────
    public DamageReport updateStatus(String id, String newStatus, String adminNotes) {
        DamageReport report = getReportById(id);
        report.setStatus(newStatus);

        if (adminNotes != null) {
            report.setAdminNotes(adminNotes);
        }

        // If resolved, set the resolved timestamp
        if ("RESOLVED".equals(newStatus)) {
            report.setResolvedAt(LocalDateTime.now());
        }

        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    // ── UPDATE RECOVERY PROGRESS ─────────────────────────────────────────
    public DamageReport updateProgress(String id, int progressPercent) {
        DamageReport report = getReportById(id);
        report.setRecoveryProgress(progressPercent);

        // Auto-resolve if 100% complete
        if (progressPercent >= 100) {
            report.setStatus("RESOLVED");
            report.setResolvedAt(LocalDateTime.now());
        } else if (progressPercent > 0) {
            report.setStatus("IN_PROGRESS");
        }

        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    // ── GET DASHBOARD STATISTICS ─────────────────────────────────────────
    public java.util.Map<String, Long> getDashboardStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", reportRepository.countActive());
        stats.put("pending", reportRepository.countByStatusAndNotDeleted("PENDING"));
        stats.put("inProgress", reportRepository.countByStatusAndNotDeleted("IN_PROGRESS"));
        stats.put("resolved", reportRepository.countByStatusAndNotDeleted("RESOLVED"));
        stats.put("rejected", reportRepository.countByStatusAndNotDeleted("REJECTED"));
        stats.put("deleted", reportRepository.countByDeletedTrue());
        return stats;
    }

    // ── SOFT DELETE REPORT (MOVE TO RECYCLE BIN) ─────────────────────────
    public DamageReport softDeleteReport(String id, String userEmail) {
        DamageReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
        report.setDeleted(true);
        report.setDeletedAt(LocalDateTime.now());
        report.setDeletedBy(userEmail != null ? userEmail : "Unknown");

        String deleterName = "Authorized Officer";
        if (userEmail != null && !userEmail.isBlank()) {
            var userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent() && userOpt.get().getFullName() != null && !userOpt.get().getFullName().isBlank()) {
                deleterName = userOpt.get().getFullName();
            } else {
                deleterName = userEmail;
            }
        }
        report.setDeletedByName(deleterName);
        report.setUpdatedAt(LocalDateTime.now());
        DamageReport saved = reportRepository.save(report);
        populatePhotoUrls(saved);
        return saved;
    }

    // ── RESTORE REPORT FROM RECYCLE BIN ──────────────────────────────────
    public DamageReport restoreReport(String id) {
        DamageReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
        report.setDeleted(false);
        report.setDeletedAt(null);
        report.setDeletedBy(null);
        report.setDeletedByName(null);
        report.setUpdatedAt(LocalDateTime.now());
        DamageReport saved = reportRepository.save(report);
        populatePhotoUrls(saved);
        return saved;
    }

    // ── PERMANENTLY DELETE REPORT ────────────────────────────────────────
    public void permanentlyDeleteReport(String id) {
        DamageReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
        // Remove photos from filesystem if present
        if (report.getPhotoUrls() != null) {
            for (String photoName : report.getPhotoUrls()) {
                try {
                    String filename = photoName;
                    if (filename.contains("/")) {
                        filename = filename.substring(filename.lastIndexOf('/') + 1);
                    }
                    File file = new File(UPLOAD_DIR + filename);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        reportRepository.deleteById(id);
    }

    // ── DELETE REPORT (LEGACY COMPATIBILITY: SOFT DELETE) ─────────────────
    public void deleteReport(String id) {
        softDeleteReport(id, null);
    }

    // ── PRIVATE HELPER: Generate Report Number ───────────────────────────
    private String generateReportNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = reportRepository.count() + 1;
        return String.format("RPT-%s-%04d", year, count);
    }

    public void populatePhotoUrls(DamageReport report) {
        if (report != null && report.getPhotoUrls() != null) {
            String baseUrl = "";
            try {
                baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            } catch (Exception e) {
                // Fallback for background or test threads without HTTP request context
                baseUrl = "http://localhost:8080";
            }
            final String finalBaseUrl = baseUrl;
            List<String> urls = report.getPhotoUrls().stream()
                    .map(filename -> {
                        if (filename.startsWith("http://") || filename.startsWith("https://") || filename.startsWith("/")) {
                            return filename;
                        }
                        return finalBaseUrl + "/api/reports/photos/" + filename;
                    })
                    .collect(java.util.stream.Collectors.toList());
            report.setPhotoUrls(urls);
        }
    }
}
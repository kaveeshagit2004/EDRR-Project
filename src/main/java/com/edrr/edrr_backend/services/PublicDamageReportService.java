package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.dto.PublicDamageReportDto;
import com.edrr.edrr_backend.model.PublicDamageReport;
import com.edrr.edrr_backend.model.SystemSetting;
import com.edrr.edrr_backend.model.User;
import com.edrr.edrr_backend.repository.PublicDamageReportRepository;
import com.edrr.edrr_backend.repository.SystemSettingRepository;
import com.edrr.edrr_backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PublicDamageReportService {

    private final PublicDamageReportRepository repository;
    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final NotificationService notificationService;

    private static final long EXPIRATION_DAYS = 7L;

    public PublicDamageReportService(PublicDamageReportRepository repository,
                                     UserRepository userRepository,
                                     SystemSettingRepository systemSettingRepository,
                                     NotificationService notificationService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.notificationService = notificationService;
    }

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/stored_photos/";

    // ── CHECK EXPIRATION (AUTO-DISAPPEAR AFTER 1 WEEK / 7 DAYS) ──────────
    public boolean isExpired(PublicDamageReport report) {
        if (report == null || report.getReportedAt() == null) return false;
        return report.getReportedAt().isBefore(LocalDateTime.now().minusDays(EXPIRATION_DAYS));
    }

    // Automatically soft-delete any reports older than 7 days
    public void autoExpireOldReports() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(EXPIRATION_DAYS);
            List<PublicDamageReport> activeReports = repository.findAllActive();
            for (PublicDamageReport r : activeReports) {
                if (r.getReportedAt() != null && r.getReportedAt().isBefore(cutoff)) {
                    r.setDeleted(true);
                    r.setDeletedAt(LocalDateTime.now());
                    r.setDeletedByName("System (Auto-Expired after 7 Days)");
                    repository.save(r);
                }
            }
        } catch (Exception e) {
            // Log silently and continue without breaking incoming requests
            System.err.println("Warning: autoExpireOldReports failed: " + e.getMessage());
        }
    }

    // Hourly background task to ensure database-level cleanliness
    @Scheduled(fixedRate = 3600000)
    public void scheduledCleanupExpiredReports() {
        autoExpireOldReports();
    }

    // ── CREATE NEW PUBLIC DAMAGE REPORT ──────────────────────────────────
    public PublicDamageReport createReport(PublicDamageReportDto dto) {
        if (!isDamageReportingOpen()) {
            throw new RuntimeException("Public damage reporting is currently closed by GA Admin. Please wait until damage reporting is opened.");
        }

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String reportNumber = generateReportNumber();
        List<String> photoUrls = new ArrayList<>();

        if (dto.getPhotos() != null) {
            for (MultipartFile file : dto.getPhotos()) {
                if (file.isEmpty()) continue;
                String cleanName = Paths.get(file.getOriginalFilename()).getFileName().toString();
                String uniqueFileName = reportNumber + "_" + UUID.randomUUID().toString() + "_" + cleanName;
                String filePath = UPLOAD_DIR + uniqueFileName;

                File dest = new File(filePath);
                try {
                    file.transferTo(dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                photoUrls.add(uniqueFileName);
            }
        }

        PublicDamageReport report = new PublicDamageReport();
        report.setReportNumber(reportNumber);
        report.setTitle(dto.getTitle());
        report.setDamageType(dto.getDamageType() != null && !dto.getDamageType().isBlank() ? dto.getDamageType() : null);
        report.setLocation(dto.getLocation());
        report.setLatitude(dto.getLatitude());
        report.setLongitude(dto.getLongitude());
        report.setSeverity(dto.getSeverity() != null && !dto.getSeverity().isBlank() ? dto.getSeverity() : null);
        report.setDescription(dto.getDescription());
        report.setAffectedArea(dto.getAffectedArea());
        report.setDivisionName(dto.getDivisionName() != null && !dto.getDivisionName().isBlank() ? dto.getDivisionName().trim() : null);

        String citizenName = dto.getReporterName();
        if (citizenName == null || citizenName.isBlank()) {
            citizenName = "Citizen Submitter";
        }
        report.setReporterName(citizenName);
        report.setReporterNic(dto.getReporterNic());
        report.setReporterPhone(dto.getReporterPhone());
        report.setReporterEmail(dto.getReporterEmail());

        report.setStatus("PENDING");
        report.setRecoveryProgress(0);
        report.setReportedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        report.setPhotoUrls(photoUrls);

        PublicDamageReport saved = repository.save(report);
        populatePhotoUrls(saved);
        try {
            notificationService.notifyCitizenReportCreated(saved);
        } catch (Exception ex) {
            System.err.println("Failed to dispatch citizen report notification: " + ex.getMessage());
        }
        return saved;
    }

    // ── GET ALL ACTIVE REPORTS (WITHIN 1 WEEK) ───────────────────────────
    public List<PublicDamageReport> getAllReports() {
        autoExpireOldReports();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(EXPIRATION_DAYS);
        List<PublicDamageReport> list = repository.findAllActive().stream()
                .filter(r -> r.getReportedAt() != null && !r.getReportedAt().isBefore(cutoff))
                .sorted((a, b) -> {
                    LocalDateTime tA = a.getReportedAt() != null ? a.getReportedAt() : LocalDateTime.MIN;
                    LocalDateTime tB = b.getReportedAt() != null ? b.getReportedAt() : LocalDateTime.MIN;
                    return tB.compareTo(tA);
                })
                .collect(Collectors.toList());
        list.forEach(this::populatePhotoUrls);
        return list;
    }

    // ── GET REPORT BY ID ─────────────────────────────────────────────────
    public PublicDamageReport getReportById(String id) {
        PublicDamageReport report = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Public report not found: " + id));
        if (report.isDeleted() || isExpired(report)) {
            throw new RuntimeException("Public report not found or has expired after 1 week: " + id);
        }
        populatePhotoUrls(report);
        return report;
    }

    // ── UPDATE STATUS ────────────────────────────────────────────────────
    public PublicDamageReport updateStatus(String id, String status, String notes) {
        PublicDamageReport report = getReportById(id);
        report.setStatus(status);
        if (notes != null && !notes.isBlank()) {
            report.setAdminNotes(notes);
        }
        report.setUpdatedAt(LocalDateTime.now());
        if ("RESOLVED".equalsIgnoreCase(status)) {
            report.setResolvedAt(LocalDateTime.now());
            report.setRecoveryProgress(100);
        } else if ("PENDING".equalsIgnoreCase(status)) {
            report.setRecoveryProgress(0);
        } else if ("IN_PROGRESS".equalsIgnoreCase(status) && report.getRecoveryProgress() == 0) {
            report.setRecoveryProgress(20);
        }
        PublicDamageReport saved = repository.save(report);
        populatePhotoUrls(saved);
        return saved;
    }

    // ── UPDATE RECOVERY PROGRESS ─────────────────────────────────────────
    public PublicDamageReport updateProgress(String id, int progress) {
        PublicDamageReport report = getReportById(id);
        int valid = Math.max(0, Math.min(100, progress));
        report.setRecoveryProgress(valid);
        report.setUpdatedAt(LocalDateTime.now());
        if (valid == 100) {
            report.setStatus("RESOLVED");
            report.setResolvedAt(LocalDateTime.now());
        } else if (valid > 0 && "PENDING".equalsIgnoreCase(report.getStatus())) {
            report.setStatus("IN_PROGRESS");
        }
        PublicDamageReport saved = repository.save(report);
        populatePhotoUrls(saved);
        return saved;
    }

    // ── SOFT DELETE REPORT (ONLY RURAL DIVISION OFFICER) ─────────────────
    public void softDeleteReport(String id, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new RuntimeException("Authentication required: Only Rural Division Officers are authorized to delete public damage reports");
        }
        if ("admin@edrr.com".equalsIgnoreCase(userEmail)) {
            throw new RuntimeException("System administrator is not authorized to delete public damage reports");
        }
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        if (!"RURAL_OFFICER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Only Rural Division Officers are authorized to delete public damage reports");
        }

        PublicDamageReport report = getReportById(id);

        // Rural Division Officers CANNOT delete until RDA/DMC officer marks the report as viewed
        boolean isViewedByRda = report.isViewedByRda()
                || "VIEWED_BY_RDA".equalsIgnoreCase(report.getStatus())
                || "VIEWED".equalsIgnoreCase(report.getStatus());

        if (!isViewedByRda) {
            throw new RuntimeException("Permission denied: Rural Division Officers cannot delete citizen reports until an RDA/DMC officer has reviewed and marked them as viewed.");
        }

        report.setDeleted(true);
        report.setDeletedAt(LocalDateTime.now());
        report.setDeletedBy(userEmail);

        String name = user.getFullName();
        report.setDeletedByName(name != null && !name.isBlank() ? name : user.getUsername());
        repository.save(report);
    }

    // ── RESTORE REPORT ───────────────────────────────────────────────────
    public PublicDamageReport restoreReport(String id) {
        PublicDamageReport report = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Public report not found: " + id));
        report.setDeleted(false);
        report.setDeletedAt(null);
        report.setDeletedBy(null);
        report.setDeletedByName(null);
        PublicDamageReport saved = repository.save(report);
        populatePhotoUrls(saved);
        return saved;
    }

    // ── PERMANENT DELETE ─────────────────────────────────────────────────
    public void permanentlyDeleteReport(String id) {
        PublicDamageReport report = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Public report not found: " + id));
        if (report.getPhotoUrls() != null) {
            for (String photoRef : report.getPhotoUrls()) {
                String filename = photoRef;
                if (photoRef.contains("/photos/")) {
                    filename = photoRef.substring(photoRef.lastIndexOf("/photos/") + 8);
                }
                File f = new File(UPLOAD_DIR + filename);
                if (f.exists()) f.delete();
            }
        }
        repository.deleteById(id);
    }

    // ── STATS (ONLY ACTIVE WITHIN 1 WEEK) ────────────────────────────────
    public Map<String, Object> getStats() {
        autoExpireOldReports();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(EXPIRATION_DAYS);
        List<PublicDamageReport> active = repository.findAllActive().stream()
                .filter(r -> r.getReportedAt() != null && !r.getReportedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        long total = active.size();
        long viewed = active.stream().filter(r -> r.isViewedByRda() || "VIEWED_BY_RDA".equalsIgnoreCase(r.getStatus())).count();
        long pending = total - viewed;
        long deleted = repository.countByDeletedTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("viewed", viewed);
        stats.put("pending", pending);
        stats.put("deleted", deleted);
        stats.put("reportingOpen", isDamageReportingOpen());
        return stats;
    }

    // ── DAMAGE REPORTING ACCESS STATUS (GA ADMIN SETTING) ───────────────
    public boolean isDamageReportingOpen() {
        return systemSettingRepository.findByKey("damage_reporting_open")
                .map(setting -> Boolean.parseBoolean(setting.getValue()))
                .orElse(false);
    }

    public SystemSetting setDamageReportingOpen(boolean open, String userEmail) {
        String updatedByName = userEmail;
        if (userEmail != null && !userEmail.isBlank()) {
            User u = userRepository.findByEmail(userEmail).orElse(null);
            if (u != null) {
                String role = u.getRole();
                if (!"GA_ADMIN".equalsIgnoreCase(role) && !"SYSTEM_ADMIN".equalsIgnoreCase(role) && !"admin@edrr.com".equalsIgnoreCase(userEmail)) {
                    throw new RuntimeException("Only Government Agent (GA) Admin is authorized to open or close damage reporting");
                }
                if (u.getFullName() != null && !u.getFullName().isBlank()) {
                    updatedByName = u.getFullName() + " (" + u.getRole() + ")";
                }
            }
        }
        SystemSetting setting = systemSettingRepository.findByKey("damage_reporting_open")
                .orElse(new SystemSetting("damage_reporting_open", String.valueOf(open), updatedByName));
        setting.setValue(String.valueOf(open));
        setting.setUpdatedAt(LocalDateTime.now());
        setting.setUpdatedBy(updatedByName);
        return systemSettingRepository.save(setting);
    }

    public SystemSetting getDamageReportingSetting() {
        return systemSettingRepository.findByKey("damage_reporting_open")
                .orElseGet(() -> new SystemSetting("damage_reporting_open", "false", "Default System Policy"));
    }

    // ── MARK AS VIEWED BY RDA / DMC ─────────────────────────────────────
    public PublicDamageReport markAsViewedByRda(String id, String userEmail, boolean viewed) {
        PublicDamageReport report = getReportById(id);
        String officerName = "RDA Admin";
        if (userEmail != null && !userEmail.isBlank()) {
            User u = userRepository.findByEmail(userEmail).orElse(null);
            if (u != null) {
                if (!"RDA_ADMIN".equalsIgnoreCase(u.getRole()) && !"admin@edrr.com".equalsIgnoreCase(userEmail)) {
                    throw new RuntimeException("Only RDA Admin is authorized to mark public damage reports as viewed");
                }
                officerName = (u.getFullName() != null && !u.getFullName().isBlank()) ? u.getFullName() : u.getUsername();
            }
        }

        report.setViewedByRda(viewed);
        if (viewed) {
            report.setStatus("VIEWED_BY_RDA");
            report.setViewedAt(LocalDateTime.now());
            report.setViewedByName(officerName);
            report.setViewedByEmail(userEmail);
        } else {
            report.setStatus("PENDING");
            report.setViewedAt(null);
            report.setViewedByName(null);
            report.setViewedByEmail(null);
        }
        report.setUpdatedAt(LocalDateTime.now());

        PublicDamageReport saved = repository.save(report);
        populatePhotoUrls(saved);
        return saved;
    }

    // ── PRIVATE: GENERATE REPORT NUMBER ──────────────────────────────────
    private String generateReportNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = repository.count() + 1;
        return String.format("PUB-%s-%04d", year, count);
    }

    // ── POPULATE PHOTO URLS ──────────────────────────────────────────────
    public void populatePhotoUrls(PublicDamageReport report) {
        if (report != null && report.getPhotoUrls() != null) {
            String baseUrl = "";
            try {
                baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            } catch (Exception e) {
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
                    .collect(Collectors.toList());
            report.setPhotoUrls(urls);
        }
    }
}

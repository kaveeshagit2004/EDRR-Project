package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.dto.PublicDamageReportDto;
import com.edrr.edrr_backend.model.PublicDamageReport;
import com.edrr.edrr_backend.model.User;
import com.edrr.edrr_backend.repository.PublicDamageReportRepository;
import com.edrr.edrr_backend.repository.UserRepository;
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

    public PublicDamageReportService(PublicDamageReportRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/stored_photos/";

    // ── CREATE NEW PUBLIC DAMAGE REPORT ──────────────────────────────────
    public PublicDamageReport createReport(PublicDamageReportDto dto) {
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
        report.setDamageType(dto.getDamageType());
        report.setLocation(dto.getLocation());
        report.setLatitude(dto.getLatitude());
        report.setLongitude(dto.getLongitude());
        report.setSeverity(dto.getSeverity() != null ? dto.getSeverity() : "MEDIUM");
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
        return saved;
    }

    // ── GET ALL ACTIVE REPORTS ───────────────────────────────────────────
    public List<PublicDamageReport> getAllReports() {
        List<PublicDamageReport> list = repository.findAllActive();
        list.sort((a, b) -> {
            LocalDateTime tA = a.getReportedAt() != null ? a.getReportedAt() : LocalDateTime.MIN;
            LocalDateTime tB = b.getReportedAt() != null ? b.getReportedAt() : LocalDateTime.MIN;
            return tB.compareTo(tA);
        });
        list.forEach(this::populatePhotoUrls);
        return list;
    }

    // ── GET REPORT BY ID ─────────────────────────────────────────────────
    public PublicDamageReport getReportById(String id) {
        PublicDamageReport report = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Public report not found: " + id));
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

    // ── SOFT DELETE REPORT ───────────────────────────────────────────────
    public void softDeleteReport(String id, String userEmail) {
        if (userEmail != null && !userEmail.isBlank()) {
            if ("admin@edrr.com".equalsIgnoreCase(userEmail)) {
                throw new RuntimeException("System administrator is not authorized to delete public damage reports");
            }
            userRepository.findByEmail(userEmail).ifPresent(u -> {
                String role = u.getRole();
                if (!List.of("RURAL_OFFICER", "GA_ADMIN", "RDA_ADMIN", "IRRIGATION_OFFICER").contains(role)) {
                    throw new RuntimeException("Role " + role + " is not authorized to delete public damage reports");
                }
            });
        }
        PublicDamageReport report = getReportById(id);
        report.setDeleted(true);
        report.setDeletedAt(LocalDateTime.now());
        report.setDeletedBy(userEmail);

        if (userEmail != null && !userEmail.isBlank()) {
            userRepository.findByEmail(userEmail).ifPresent(u -> {
                String name = u.getFullName();
                report.setDeletedByName(name != null && !name.isBlank() ? name : u.getUsername());
            });
        }
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

    // ── STATS ────────────────────────────────────────────────────────────
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", repository.countActive());
        stats.put("pending", repository.countByStatusAndNotDeleted("PENDING"));
        stats.put("inProgress", repository.countByStatusAndNotDeleted("IN_PROGRESS"));
        stats.put("resolved", repository.countByStatusAndNotDeleted("RESOLVED"));
        stats.put("deleted", repository.countByDeletedTrue());
        return stats;
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

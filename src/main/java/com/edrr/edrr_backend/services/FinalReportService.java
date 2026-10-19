package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.model.FinalReport;
import com.edrr.edrr_backend.repository.FinalReportRepository;
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
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/stored_final_reports/";

    public FinalReportService(FinalReportRepository finalReportRepository) {
        this.finalReportRepository = finalReportRepository;
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
        return finalReportRepository.findAllByOrderByUploadedAtDesc();
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

    public void deleteFinalReport(String id) {
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
}

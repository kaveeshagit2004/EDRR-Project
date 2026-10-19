package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "final_reports")
public class FinalReport {

    @Id
    private String id;

    // Original file name uploaded by the user, e.g. "Matara_Bypass_Reconstruction.pdf"
    private String fileName;

    // Unique file name stored on server disk, e.g. "final_report_UUID_Matara_Bypass.pdf"
    private String storedFileName;

    // MIME type, e.g. "application/pdf" or "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    private String fileType;

    // Size of the file in bytes
    private long fileSize;

    // Damage type: ROAD_DAMAGE, BRIDGE_DAMAGE, FLOOD, WATER_POLLUTION, BUILDING, OTHER
    private String damageType;

    // Closure Summary / recovery outcome notes
    private String notes;

    // Officer who uploaded the final report (e.g. RDA Officer name)
    private String uploadedBy;

    // Role of the uploader (e.g. "RDA_ADMIN")
    private String uploadedByRole;

    // Relative download URL, e.g. "/api/final-reports/download/6ad5..."
    private String downloadUrl;

    // Timestamp when uploaded
    private LocalDateTime uploadedAt;

    public FinalReport() {
    }

    public FinalReport(String id, String fileName, String storedFileName, String fileType, long fileSize, String damageType, String notes, String uploadedBy, String uploadedByRole, String downloadUrl, LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.damageType = damageType;
        this.notes = notes;
        this.uploadedBy = uploadedBy;
        this.uploadedByRole = uploadedByRole;
        this.downloadUrl = downloadUrl;
        this.uploadedAt = uploadedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getUploadedByRole() { return uploadedByRole; }
    public void setUploadedByRole(String uploadedByRole) { this.uploadedByRole = uploadedByRole; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}

package com.edrr.edrr_backend.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class PublicDamageReportDto {

    private String id;
    private String title;
    private String damageType;
    private String location;
    private double latitude;
    private double longitude;
    private String severity;
    private String description;
    private String affectedArea;
    private String divisionName;

    // Citizen submitter contact details
    private String reporterName;
    private String reporterNic;
    private String reporterPhone;
    private String reporterEmail;

    private List<MultipartFile> photos;

    public PublicDamageReportDto() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAffectedArea() { return affectedArea; }
    public void setAffectedArea(String affectedArea) { this.affectedArea = affectedArea; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterNic() { return reporterNic; }
    public void setReporterNic(String reporterNic) { this.reporterNic = reporterNic; }

    public String getReporterPhone() { return reporterPhone; }
    public void setReporterPhone(String reporterPhone) { this.reporterPhone = reporterPhone; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public List<MultipartFile> getPhotos() { return photos; }
    public void setPhotos(List<MultipartFile> photos) { this.photos = photos; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }
}

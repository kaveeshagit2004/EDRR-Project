package com.edrr.edrr_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

                          // Lombok: auto-generates getters, setters, toString
             // Lombok: generates empty constructor
            // Lombok: generates constructor with all fields
@Document(collection = "users") // MongoDB collection name
public class User {

    @Id
    private String id;         // MongoDB auto-generates this (like primary key)

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Indexed(unique = true)    // Username must be unique in database
    private String username;

    private String profilePhotoUrl;

    @NotBlank(message = "NIC is required")
    @Indexed(unique = true)
    private String nic;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;   // This will be stored as encrypted hash

    // Role must be one of: CITIZEN, RURAL_OFFICER, IRRIGATION_OFFICER, GA_ADMIN, RDA_ADMIN
    private String role;

    private String phoneNumber;
    private String address;

    // Rural Officer Division Name (specific to RURAL_OFFICER)
    private String divisionName;

    // Assigned Water-Reading Station (specific to IRRIGATION_OFFICER)
    private String waterStation;

    private boolean active = true;   // Account active status
    private boolean verified = false; // Email verified status

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    // Soft delete / recycle bin
    private boolean deleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private String deletedByName;
    private String deleteReason;

    public User() {
    }

    public User(String id, String fullName, String username, String nic, String email, String password, String role, String phoneNumber, String address, boolean active, boolean verified, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLogin) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.nic = nic;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.active = active;
        this.verified = verified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }
    public String getWaterStation() { return waterStation; }
    public void setWaterStation(String waterStation) { this.waterStation = waterStation; }
    public String getWaterReadingStation() { return waterStation; }
    public void setWaterReadingStation(String waterReadingStation) { this.waterStation = waterReadingStation; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
    public String getDeletedByName() { return deletedByName; }
    public void setDeletedByName(String deletedByName) { this.deletedByName = deletedByName; }
    public String getDeleteReason() { return deleteReason; }
    public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }
}

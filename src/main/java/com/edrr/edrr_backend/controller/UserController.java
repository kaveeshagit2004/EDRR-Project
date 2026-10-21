package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.model.User;
import com.edrr.edrr_backend.model.WaterLevel;
import com.edrr.edrr_backend.repository.WaterLevelRepository;
import com.edrr.edrr_backend.services.UserServices;
import com.edrr.edrr_backend.services.WaterLevelService;
import org.springframework.context.annotation.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserServices userService;
    private final WaterLevelRepository waterLevelRepository;
    private final WaterLevelService waterLevelService;

    public UserController(UserServices userService, WaterLevelRepository waterLevelRepository, WaterLevelService waterLevelService) {
        this.userService = userService;
        this.waterLevelRepository = waterLevelRepository;
        this.waterLevelService = waterLevelService;
    }

    // ── GET ALL OFFICERS ─────────────────────────────────────────────────
    @GetMapping("/officers")
    public ResponseEntity<List<User>> getOfficers() {
        List<User> officers = new ArrayList<>();
        officers.addAll(userService.getUsersByRole("RURAL_OFFICER"));
        officers.addAll(userService.getUsersByRole("IRRIGATION_OFFICER"));
        officers.addAll(userService.getUsersByRole("GA_ADMIN"));
        officers.addAll(userService.getUsersByRole("RDA_ADMIN"));
        
        // Exclude any deleted officers
        officers.removeIf(User::isDeleted);

        // Remove password from response for security
        officers.forEach(user -> user.setPassword(null));
        
        return ResponseEntity.ok(officers);
    }

    // ── GET RECYCLE BIN (DELETED OFFICERS/USERS) ─────────────────────────
    @GetMapping("/recycle-bin")
    public ResponseEntity<List<User>> getRecycleBin(Principal principal) {
        String callerEmail = principal != null ? principal.getName() : "admin@edrr.com";
        return ResponseEntity.ok(userService.getDeletedUsers(callerEmail));
    }

    // ── GET RURAL DIVISIONS (FOR PUBLIC REPORT FILTERING & INTAKE) ────────
    // GET http://localhost:8080/api/users/rural-divisions
    @GetMapping("/rural-divisions")
    public ResponseEntity<List<String>> getRuralDivisions() {
        List<String> divisions = userService.getUsersByRole("RURAL_OFFICER").stream()
                .filter(u -> !u.isDeleted())
                .map(User::getDivisionName)
                .filter(d -> d != null && !d.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return ResponseEntity.ok(divisions);
    }

    // ── GET WATER STATIONS (FOR IRRIGATION OFFICER ASSIGNMENT) ────────────
    // GET http://localhost:8080/api/users/water-stations
    @GetMapping("/water-stations")
    public ResponseEntity<List<String>> getWaterStations() {
        List<String> stations = new ArrayList<>();
        
        // From existing water_levels collection
        waterLevelRepository.findAll().forEach(wl -> {
            if (wl.getLocationName() != null && !wl.getLocationName().isBlank()) {
                stations.add(wl.getLocationName().trim());
            }
        });

        // From registered IRRIGATION_OFFICER accounts
        userService.getUsersByRole("IRRIGATION_OFFICER").stream()
                .filter(u -> !u.isDeleted())
                .forEach(u -> {
                    if (u.getWaterStation() != null && !u.getWaterStation().isBlank()) {
                        stations.add(u.getWaterStation().trim());
                    }
                });

        List<String> distinct = stations.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return ResponseEntity.ok(distinct);
    }

    // ── ADD NEW OFFICER (JSON) ───────────────────────────────────────────
    @PostMapping(value = "/officers", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOfficerJson(@Valid @RequestBody User user) {
        return registerOfficerInternal(user, null);
    }

    // ── ADD NEW OFFICER (MULTIPART / WITH OPTIONAL PHOTO) ─────────────────
    @PostMapping(value = "/officers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOfficerMultipart(
            @Valid @ModelAttribute User user,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto
    ) {
        return registerOfficerInternal(user, profilePhoto);
    }

    private ResponseEntity<?> registerOfficerInternal(User user, MultipartFile profilePhoto) {
        try {
            // Validate that the role is actually a valid registry role
            String role = user.getRole();
            if (!"RURAL_OFFICER".equals(role) && !"IRRIGATION_OFFICER".equals(role) && !"GA_ADMIN".equals(role) && !"RDA_ADMIN".equals(role)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid role. Only RURAL_OFFICER, IRRIGATION_OFFICER, GA_ADMIN, and RDA_ADMIN are allowed.");
                return ResponseEntity.badRequest().body(error);
            }

            // Save profile photo if provided
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                String photoUrl = saveProfilePhoto(profilePhoto);
                if (photoUrl != null) {
                    user.setProfilePhotoUrl(photoUrl);
                }
            }

            User savedUser = userService.registerUser(user);

            // If IRRIGATION_OFFICER, assign station and associate/initialize WaterLevel record
            if ("IRRIGATION_OFFICER".equals(savedUser.getRole()) && savedUser.getWaterStation() != null && !savedUser.getWaterStation().isBlank()) {
                String stationName = savedUser.getWaterStation().trim();
                Optional<WaterLevel> existingOpt = waterLevelRepository.findByLocationNameIgnoreCase(stationName);
                if (existingOpt.isPresent()) {
                    WaterLevel existing = existingOpt.get();
                    existing.setOfficerId(savedUser.getId());
                    existing.setOfficerName(savedUser.getFullName());
                    existing.setReportedBy(savedUser.getId());
                    existing.setReporterName(savedUser.getFullName());
                    existing.setUpdatedBy(savedUser.getId());
                    existing.setUpdatedByName(savedUser.getFullName());
                    existing.setLastUpdated(LocalDateTime.now());
                    waterLevelRepository.save(existing);
                } else {
                    WaterLevel newStation = new WaterLevel();
                    newStation.setLocationName(stationName);
                    newStation.setDistrict("Matara");
                    newStation.setCurrentLevel(0.0);
                    newStation.setMaximumLimit(5.0);
                    newStation.setStatus("NORMAL");
                    newStation.setTrend("STABLE");
                    newStation.setPercentageOfMax(0.0);
                    newStation.setOfficerId(savedUser.getId());
                    newStation.setOfficerName(savedUser.getFullName());
                    newStation.setReportedBy(savedUser.getId());
                    newStation.setReporterName(savedUser.getFullName());
                    newStation.setUpdatedBy(savedUser.getId());
                    newStation.setUpdatedByName(savedUser.getFullName());
                    newStation.setCreatedAt(LocalDateTime.now());
                    newStation.setLastUpdated(LocalDateTime.now());
                    waterLevelService.addLocation(newStation);
                }
            }

            savedUser.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Officer registered successfully!");
            response.put("user", savedUser);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String saveProfilePhoto(MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/stored_photos/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String originalName = file.getOriginalFilename();
            String cleanName = originalName != null ? Paths.get(originalName).getFileName().toString() : "photo.jpg";
            String uniqueFileName = "officer_" + UUID.randomUUID().toString() + "_" + cleanName;
            File dest = new File(uploadDir + uniqueFileName);
            file.transferTo(dest);
            return "/api/reports/photos/" + uniqueFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── SOFT DELETE OFFICER (MOVE TO RECYCLE BIN WITH REASON) ─────────────
    @DeleteMapping("/officers/{id}")
    public ResponseEntity<?> deleteOfficer(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(value = "reason", required = false) String paramReason,
            Principal principal
    ) {
        try {
            String reason = null;
            if (body != null && body.containsKey("reason")) {
                reason = body.get("reason");
            }
            if ((reason == null || reason.isBlank()) && paramReason != null) {
                reason = paramReason;
            }
            if (reason == null || reason.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "A reason for deleting this officer is required.");
                return ResponseEntity.badRequest().body(error);
            }

            String callerEmail = principal != null ? principal.getName() : "admin@edrr.com";
            User deletedUser = userService.softDeleteUser(id, callerEmail, reason.trim());
            deletedUser.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Officer moved to recycle bin successfully!");
            response.put("user", deletedUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── RESTORE OFFICER FROM RECYCLE BIN ─────────────────────────────────
    @PutMapping("/officers/{id}/restore")
    public ResponseEntity<?> restoreOfficer(@PathVariable String id, Principal principal) {
        try {
            String callerEmail = principal != null ? principal.getName() : "admin@edrr.com";
            User restoredUser = userService.restoreUser(id, callerEmail);
            restoredUser.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Officer restored successfully!");
            response.put("user", restoredUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── PERMANENTLY DELETE OFFICER ───────────────────────────────────────
    @DeleteMapping("/officers/{id}/permanent")
    public ResponseEntity<?> permanentlyDeleteOfficer(@PathVariable String id, Principal principal) {
        try {
            String callerEmail = principal != null ? principal.getName() : "admin@edrr.com";
            userService.permanentlyDeleteUser(id, callerEmail);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Officer permanently deleted.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── UPDATE USER PROFILE PHOTO ────────────────────────────────────────
    // POST http://localhost:8080/api/users/profile-photo
    @PostMapping(value = "/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "email", required = false) String email,
            Principal principal
    ) {
        try {
            if (photo == null || photo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please provide a valid image file."));
            }

            String identifier = principal != null && principal.getName() != null && !principal.getName().isBlank()
                    ? principal.getName()
                    : (userId != null && !userId.isBlank() ? userId : email);

            if (identifier == null || identifier.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User authentication or identification required."));
            }

            String photoUrl = saveProfilePhoto(photo);
            if (photoUrl == null) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save profile photo on server."));
            }

            User updated = userService.updateProfilePhoto(identifier, photoUrl);
            updated.setPassword(null);

            Map<String, Object> res = new HashMap<>();
            res.put("message", "Profile photo updated successfully!");
            res.put("profilePhotoUrl", photoUrl);
            res.put("user", updated);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error uploading profile photo: " + e.getMessage()));
        }
    }
}

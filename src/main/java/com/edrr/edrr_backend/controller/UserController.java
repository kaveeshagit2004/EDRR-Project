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

   /* @GetMapping("")
    public ResponseEntity  getUsers(){
        return ResponseEntity.ok(null);
    } */


    // ── GET ALL OFFICERS ─────────────────────────────────────────────────
    @GetMapping("/officers")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<List<User>> getOfficers() {
        List<User> officers = new ArrayList<>();
        officers.addAll(userService.getUsersByRole("RURAL_OFFICER"));
        officers.addAll(userService.getUsersByRole("IRRIGATION_OFFICER"));
        officers.addAll(userService.getUsersByRole("GA_ADMIN"));
        officers.addAll(userService.getUsersByRole("RDA_ADMIN"));
        
        // Remove password from response for security
        officers.forEach(user -> user.setPassword(null));
        
        return ResponseEntity.ok(officers);
    }

    // ── GET RURAL DIVISIONS (FOR PUBLIC REPORT FILTERING & INTAKE) ────────
    // GET http://localhost:8080/api/users/rural-divisions
    @GetMapping("/rural-divisions")
    public ResponseEntity<List<String>> getRuralDivisions() {
        List<String> divisions = userService.getUsersByRole("RURAL_OFFICER").stream()
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
        userService.getUsersByRole("IRRIGATION_OFFICER").forEach(u -> {
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

    // ── DELETE OFFICER ───────────────────────────────────────────────────
    @DeleteMapping("/officers/{id}")
    public ResponseEntity<?> deleteOfficer(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Officer deleted successfully!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

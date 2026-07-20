package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.model.User;
import com.edrr.edrr_backend.services.UserServices;
import org.springframework.context.annotation.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserServices userService;

    public UserController(UserServices userService) {
        this.userService = userService;
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

    // ── ADD NEW OFFICER ──────────────────────────────────────────────────
    @PostMapping("/officers")
    public ResponseEntity<?> createOfficer(@Valid @RequestBody User user) {
        try {
            // Validate that the role is actually a valid registry role
            String role = user.getRole();
            if (!"RURAL_OFFICER".equals(role) && !"IRRIGATION_OFFICER".equals(role) && !"GA_ADMIN".equals(role) && !"RDA_ADMIN".equals(role)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid role. Only RURAL_OFFICER, IRRIGATION_OFFICER, GA_ADMIN, and RDA_ADMIN are allowed.");
                return ResponseEntity.badRequest().body(error);
            }

            User savedUser = userService.registerUser(user);
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

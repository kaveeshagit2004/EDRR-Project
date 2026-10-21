package com.edrr.edrr_backend.controller;

import com.edrr.edrr_backend.config.JwtUtil;
import com.edrr.edrr_backend.model.User;
import com.edrr.edrr_backend.services.UserServices;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // Allow requests from any frontend
public class AuthController {
    public AuthController(UserServices userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    private final UserServices userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // ── REGISTER ─────────────────────────────────────────────────────────
    // POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);

            // Don't return the password in response
            savedUser.setPassword(null);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful!");
            response.put("user", savedUser);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────
    // POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Get user info and check soft delete / deactivation status
            User user = userService.getUserByEmail(email).orElse(null);
            if (user != null && (user.isDeleted() || !user.isActive())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "This account has been deactivated or moved to the recycle bin. Please contact the User Administrator.");
                return ResponseEntity.status(403).body(error);
            }

            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

            // Update last login timestamp in DB
            userService.updateLastLogin(email);

            if (user != null) {
                user.setPassword(null); // Don't send password back
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("message", "Login successful!");
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(401).body(error);
        }
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────────────
    // POST http://localhost:8080/api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String nic = request.get("nic");
            String newPassword = request.get("newPassword");

            if (email == null || nic == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email, NIC, and new password are required"));
            }

            java.util.Optional<User> userOpt = userService.getUserByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found with this email"));
            }

            User user = userOpt.get();
            if (user.getNic() == null || !nic.equalsIgnoreCase(user.getNic())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid NIC provided for this email"));
            }

            userService.resetPassword(email, newPassword);

            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

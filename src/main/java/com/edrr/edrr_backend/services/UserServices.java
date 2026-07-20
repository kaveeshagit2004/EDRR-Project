package com.edrr.edrr_backend.services;

import com.edrr.edrr_backend.model.User;
import com.edrr.edrr_backend.repository.UserRepository         ;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
  // Lombok: auto-generates constructor for final fields
public class UserServices {
    public UserServices(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── REGISTER A NEW USER ──────────────────────────────────────────────
    public User registerUser(User user) {

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken: " + user.getUsername());
        }

        // Check if NIC already exists
        if (userRepository.existsByNic(user.getNic())) {
            throw new RuntimeException("NIC already registered: " + user.getNic());
        }

        // Encrypt the password before saving (NEVER save plain text password!)
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        // Save to MongoDB
        return userRepository.save(user);
    }

    // ── GET USER BY ID ───────────────────────────────────────────────────
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // ── GET USER BY EMAIL ────────────────────────────────────────────────
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ── GET ALL USERS ────────────────────────────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ── GET USERS BY ROLE ────────────────────────────────────────────────
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // ── UPDATE USER PROFILE ──────────────────────────────────────────────
    public User updateUser(String id, User updatedUser) {
        User existingUser = getUserById(id);

        // Only update allowed fields
        if (updatedUser.getFullName() != null) {
            existingUser.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getAddress() != null) {
            existingUser.setAddress(updatedUser.getAddress());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    // ── DEACTIVATE USER ──────────────────────────────────────────────────
    public void deactivateUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ── DELETE USER ──────────────────────────────────────────────────────
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ── UPDATE LAST LOGIN TIMESTAMP ──────────────────────────────────────
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
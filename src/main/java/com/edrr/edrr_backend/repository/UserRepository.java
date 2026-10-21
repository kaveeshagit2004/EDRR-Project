package com.edrr.edrr_backend.repository;

import com.edrr.edrr_backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Find user by email (for login)
    Optional<User> findByEmail(String email);

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by NIC
    Optional<User> findByNic(String nic);

    // Find all users by role
    List<User> findByRole(String role);

    // Active (non-deleted) users
    @Query("{ '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }")
    List<User> findAllActive();

    // Soft-deleted users (in recycle bin)
    @Query("{ 'deleted': true }")
    List<User> findByDeletedTrue();

    // Active users by role
    @Query("{ 'role': ?0, '$or': [ { 'deleted': false }, { 'deleted': { '$exists': false } } ] }")
    List<User> findByRoleAndNotDeleted(String role);

    @Query(value = "{ 'deleted': true }", count = true)
    long countByDeletedTrue();

    // Check if email already exists
    boolean existsByEmail(String email);

    // Check if username already exists
    boolean existsByUsername(String username);

    // Check if NIC already exists
    boolean existsByNic(String nic);
}

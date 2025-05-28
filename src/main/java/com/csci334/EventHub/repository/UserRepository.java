package com.csci334.EventHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.Role;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    // Find a user by their email address
    Optional<User> findByEmail(String email);

    // Check existence by email instead of username
    boolean existsByEmail(String email);

    // Find all users who have a given role
    List<User> findByRole(Role role);
}
package com.csci334.EventHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.Role;
import com.csci334.EventHub.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getById(String id) {
        return repo.findById(id);
    }

    public Optional<User> getByEmail(String email) {
        return repo.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return repo.existsByEmail(email);
    }

    public List<User> getByRole(Role role) {
        return repo.findByRole(role);
    }

    @Transactional
    public User create(User user) {
        return repo.save(user);
    }

    @Transactional
    public User update(String id, User updated) {
        updated.setId(id);
        return repo.save(updated);
    }

    @Transactional
    public void delete(String id) {
        repo.deleteById(id);
    }

    @Transactional
    public User updateProfile(String id, String firstName, String lastName) {
        User user = getById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return repo.save(user);
    }

    @Transactional
    public void updatePassword(String id, String currentPassword, String newPassword) {
        User user = getById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalStateException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        repo.save(user);
    }
}
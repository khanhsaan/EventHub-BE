package com.csci334.EventHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.csci334.EventHub.dto.LoginRequest;
import com.csci334.EventHub.dto.SignupRequest;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Transactional
    public User signup(SignupRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        u.setRole(req.getRole());

        return userRepo.save(u);
    }

    public User login(LoginRequest req) {
        User u = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return u;
    }
}

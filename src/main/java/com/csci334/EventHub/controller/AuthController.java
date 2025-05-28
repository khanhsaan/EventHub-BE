package com.csci334.EventHub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.csci334.EventHub.dto.AuthResponse;
import com.csci334.EventHub.dto.LoginRequest;
import com.csci334.EventHub.dto.SignupRequest;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authSvc;

    public AuthController(AuthService authSvc) {
        this.authSvc = authSvc;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest req) {
        User u = authSvc.signup(req);
        AuthResponse resp = toResponse(u);
        return ResponseEntity
                .created(null) // could point to /api/users/{id}
                .body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        User u = authSvc.login(req);
        return ResponseEntity.ok(toResponse(u));
    }

    private AuthResponse toResponse(User u) {
        AuthResponse r = new AuthResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setFirstName(u.getFirstName());
        r.setLastName(u.getLastName());
        r.setRole(u.getRole());
        return r;
    }
}

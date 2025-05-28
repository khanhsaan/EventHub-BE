package com.csci334.EventHub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.csci334.EventHub.dto.PasswordUpdateRequest;
import com.csci334.EventHub.dto.ProfileUpdateRequest;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.Role;
import com.csci334.EventHub.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService svc;

    public UserController(UserService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<User> all() {
        return svc.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> one(@PathVariable String id) {
        return svc.getById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<User> byEmail(@RequestParam String email) {
        return svc.getByEmail(email).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-role/{role}")
    public List<User> byRole(@PathVariable Role role) {
        return svc.getByRole(role);
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        User created = svc.create(user);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String id,
            @RequestBody User user) {
        return ResponseEntity.ok(svc.update(id, user));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<User> updateProfile(@PathVariable String id, @RequestBody ProfileUpdateRequest request) {
        User updated = svc.updateProfile(id, request.getFirstName(), request.getLastName());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable String id, @RequestBody PasswordUpdateRequest request) {
        svc.updatePassword(id, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}

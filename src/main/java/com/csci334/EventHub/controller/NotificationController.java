package com.csci334.EventHub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.csci334.EventHub.entity.Notification;
import com.csci334.EventHub.service.NotificationService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService svc;

    public NotificationController(NotificationService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<Notification> all() {
        return svc.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> one(@PathVariable Long id) {
        return svc.getById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Notification> forUser(@PathVariable String userId) {
        return svc.getForUser(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<Notification> unread(@PathVariable String userId) {
        return svc.getUnread(userId);
    }

    @GetMapping("/event/{eventId}")
    public List<Notification> byEvent(@PathVariable String eventId) {
        return svc.getByEvent(eventId);
    }

    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Notification n) {
        Notification created = svc.create(n);
        return ResponseEntity.created(URI.create("/api/notifications/" + created.getId()))
                .body(created);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        svc.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}

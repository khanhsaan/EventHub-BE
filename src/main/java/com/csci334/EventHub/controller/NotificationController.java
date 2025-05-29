package com.csci334.EventHub.controller;

import com.csci334.EventHub.dto.NotificationDTO;
import com.csci334.EventHub.entity.Notification;
import com.csci334.EventHub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/events/{eventId}/send-reminders")
    public ResponseEntity<String> sendReminders(@PathVariable String eventId) {
        log.debug("Received request to send reminders for eventId: {}", eventId);
        notificationService.sendReminderToRegistrants(eventId);
        return ResponseEntity.ok("Reminders sent successfully.");
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable String userId) {
        List<NotificationDTO> notis = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notis);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

}

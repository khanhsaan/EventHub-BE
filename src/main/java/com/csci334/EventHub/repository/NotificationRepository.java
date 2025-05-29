package com.csci334.EventHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.csci334.EventHub.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Unread notifications for a user
    List<Notification> findByRecipientIdAndIsReadFalse(String recipientId);

    // All notifications for a user, sorted newest first
    List<Notification> findByRecipientIdOrderBySentAtDesc(String recipientId);

    // Notifications related to a specific event
    List<Notification> findByEventId(String eventId);

    List<Notification> findByRecipientId(String userId);
}

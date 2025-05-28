package com.csci334.EventHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.csci334.EventHub.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Unread notifications for a user
    List<Notification> findByRecipient_IdAndIsReadFalse(String recipientId);

    // All notifications for a user, sorted newest first
    List<Notification> findByRecipient_IdOrderBySentAtDesc(String recipientId);

    // Notifications related to a specific event
    List<Notification> findByEvent_Id(String eventId);
}

package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.NotificationDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Notification;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.NotificationRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendReminderToRegistrants(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        log.debug("Fetching registrations for event: {}", event.getTitle());
        List<Registration> registrations = registrationRepository.findByEventId(eventId);

        for (Registration reg : registrations) {
            User recipient = reg.getAttendee();
            log.debug("Sending notification to user: {}", recipient.getEmail());

            Notification notification = new Notification();
            String message = "Reminder: You are registered for the event '" + event.getTitle()
                    + "'. This event will be held on " + event.getEventDate() + " " + event.getStartTime() + " at "
                    + event.getLocation() + ".";
            notification.setTitle("Event Reminder: " + event.getTitle());
            notification.setMessage(message);
            notification.setSentAt(LocalDateTime.now());
            notification.setRead(false);
            notification.setRecipient(recipient);
            notification.setEvent(event);

            Notification saved = notificationRepository.save(notification);

            // ✅ Send real-time message to this user
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + recipient.getId(),
                    saved);
        }

        log.info("Reminders sent to {} registrants for event '{}'", registrations.size(), event.getTitle());
    }

    public List<NotificationDTO> getNotificationsByUserId(String userId) {
        List<Notification> notifications = notificationRepository.findByRecipientId(userId);

        return notifications.stream()
                .map(n -> new NotificationDTO(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getSentAt(),
                        n.isRead(),
                        n.getRecipient().getFullName()

                )).toList();
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public NotificationDTO convertToDto(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setSentAt(n.getSentAt());
        dto.setRead(n.isRead());
        dto.setRecipientName(n.getRecipient().getFullName());
        return dto;
    }

}

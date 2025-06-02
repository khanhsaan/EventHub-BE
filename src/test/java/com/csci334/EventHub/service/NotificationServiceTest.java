package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.NotificationDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Notification;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.EventStatus;
import com.csci334.EventHub.entity.enums.EventType;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.Role;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.NotificationRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import com.csci334.EventHub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private Event testEvent;
    private User testUser1;
    private User testUser2;
    private Registration testRegistration1;
    private Registration testRegistration2;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = new User();
        testUser1.setId("123456");
        testUser1.setEmail("user1@test.com");
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setRole(Role.ATTENDEE);

        testUser2 = new User();
        testUser2.setId("789012");
        testUser2.setEmail("user2@test.com");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setRole(Role.ATTENDEE);

        // Create test event
        testEvent = new Event();
        testEvent.setId("EVENT1");
        testEvent.setTitle("Spring Conference 2025");
        testEvent.setDescription("A wonderful spring conference");
        testEvent.setEventDate(LocalDate.of(2025, 6, 15));
        testEvent.setStartTime(LocalTime.of(9, 0));
        testEvent.setEndTime(LocalTime.of(17, 0));
        testEvent.setLocation("Convention Center");
        testEvent.setEventType(EventType.CONFERENCE);
        testEvent.setStatus(EventStatus.PUBLISHED);
        testEvent.setOrganizer(testUser1);

        // Create test registrations
        testRegistration1 = new Registration();
        testRegistration1.setId("REG001");
        testRegistration1.setEvent(testEvent);
        testRegistration1.setAttendee(testUser1);
        testRegistration1.setStatus(RegistrationStatus.PAID);

        testRegistration2 = new Registration();
        testRegistration2.setId("REG002");
        testRegistration2.setEvent(testEvent);
        testRegistration2.setAttendee(testUser2);
        testRegistration2.setStatus(RegistrationStatus.PAID);

        // Create test notification
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("Test message");
        testNotification.setSentAt(LocalDateTime.now());
        testNotification.setRead(false);
        testNotification.setRecipient(testUser1);
        testNotification.setEvent(testEvent);
    }

    @Test
    void sendReminderToRegistrants_ShouldSendNotificationsToAllRegistrants() {
        // Arrange
        String eventId = "EVENT1";
        List<Registration> registrations = Arrays.asList(testRegistration1, testRegistration2);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.findByEventId(eventId)).thenReturn(registrations);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        // Act
        notificationService.sendReminderToRegistrants(eventId);

        // Assert
        verify(eventRepository).findById(eventId);
        verify(registrationRepository).findByEventId(eventId);
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Notification.class));

        // Capture and verify all saved notifications
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        List<Notification> savedNotifications = notificationCaptor.getAllValues();
        assertEquals(2, savedNotifications.size());

        // Verify each notification has correct content
        for (Notification notification : savedNotifications) {
            assertEquals("Event Reminder: Spring Conference 2025", notification.getTitle());
            assertTrue(notification.getMessage().contains("Spring Conference 2025"));
            assertTrue(notification.getMessage().contains("2025-06-15"));
            assertTrue(notification.getMessage().contains("09:00"));
            assertTrue(notification.getMessage().contains("Convention Center"));
            assertFalse(notification.isRead());
            assertNotNull(notification.getSentAt());
            assertEquals(testEvent, notification.getEvent());
        }
    }

    @Test
    void sendReminderToRegistrants_ShouldThrowException_WhenEventNotFound() {
        // Arrange
        String eventId = "NONEXISTENT";
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> notificationService.sendReminderToRegistrants(eventId));

        assertEquals("Event not found with ID: NONEXISTENT", exception.getMessage());
        verify(eventRepository).findById(eventId);
        verify(registrationRepository, never()).findByEventId(anyString());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void sendReminderToRegistrants_ShouldHandleEmptyRegistrations() {
        // Arrange
        String eventId = "EVENT1";
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.findByEventId(eventId)).thenReturn(new ArrayList<>());

        // Act
        notificationService.sendReminderToRegistrants(eventId);

        // Assert
        verify(eventRepository).findById(eventId);
        verify(registrationRepository).findByEventId(eventId);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void getNotificationsByUserId_ShouldReturnListOfNotificationDTOs() {
        // Arrange
        String userId = "123456";
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByRecipientId(userId)).thenReturn(notifications);

        // Act
        List<NotificationDTO> result = notificationService.getNotificationsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        NotificationDTO dto = result.get(0);
        assertEquals(testNotification.getId(), dto.getId());
        assertEquals(testNotification.getTitle(), dto.getTitle());
        assertEquals(testNotification.getMessage(), dto.getMessage());
        assertEquals(testNotification.getSentAt(), dto.getSentAt());
        assertEquals(testNotification.isRead(), dto.isRead());
        assertEquals("John Doe", dto.getRecipientName());

        verify(notificationRepository).findByRecipientId(userId);
    }

    @Test
    void getNotificationsByUserId_ShouldReturnEmptyList_WhenNoNotifications() {
        // Arrange
        String userId = "123456";
        when(notificationRepository.findByRecipientId(userId)).thenReturn(new ArrayList<>());

        // Act
        List<NotificationDTO> result = notificationService.getNotificationsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository).findByRecipientId(userId);
    }

    @Test
    void markAsRead_ShouldMarkNotificationAsRead() {
        // Arrange
        Long notificationId = 1L;
        testNotification.setRead(false);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(argThat(notification -> notification.isRead()));
    }

    @Test
    void markAsRead_ShouldThrowException_WhenNotificationNotFound() {
        // Arrange
        Long notificationId = 999L;
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> notificationService.markAsRead(notificationId));

        assertEquals("Notification not found", exception.getMessage());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void sendNotification_ShouldCreateAndSendNotification() {
        // Arrange
        String userId = "123456";
        String title = "Test Title";
        String message = "Test Message";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        // Act
        notificationService.sendNotification(userId, title, message);

        // Assert
        verify(userRepository).findById(userId);
        verify(notificationRepository).save(argThat(notification ->
                notification.getTitle().equals(title) &&
                        notification.getMessage().equals(message) &&
                        !notification.isRead() &&
                        notification.getRecipient().equals(testUser1) &&
                        notification.getSentAt() != null
        ));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications/" + userId),
                any(NotificationDTO.class)
        );
    }

    @Test
    void sendNotification_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String userId = "NONEXISTENT";
        String title = "Test Title";
        String message = "Test Message";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> notificationService.sendNotification(userId, title, message));

        verify(userRepository).findById(userId);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void convertToDto_ShouldConvertNotificationToDTO() {
        // Act
        NotificationDTO result = notificationService.convertToDto(testNotification);

        // Assert
        assertNotNull(result);
        assertEquals(testNotification.getId(), result.getId());
        assertEquals(testNotification.getTitle(), result.getTitle());
        assertEquals(testNotification.getMessage(), result.getMessage());
        assertEquals(testNotification.getSentAt(), result.getSentAt());
        assertEquals(testNotification.isRead(), result.isRead());
        assertEquals("John Doe", result.getRecipientName());
    }

    @Test
    void convertToDto_ShouldHandleNullFields() {
        // Arrange
        Notification notificationWithNulls = new Notification();
        notificationWithNulls.setId(1L);
        notificationWithNulls.setRecipient(testUser1);

        // Act
        NotificationDTO result = notificationService.convertToDto(notificationWithNulls);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getTitle());
        assertNull(result.getMessage());
        assertNull(result.getSentAt());
        assertFalse(result.isRead());
        assertEquals("John Doe", result.getRecipientName());
    }

    @Test
    void sendReminderToRegistrants_ShouldSendCorrectWebSocketMessages() {
        // Arrange
        String eventId = "EVENT1";
        List<Registration> registrations = Arrays.asList(testRegistration1, testRegistration2);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.findByEventId(eventId)).thenReturn(registrations);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        // Act
        notificationService.sendReminderToRegistrants(eventId);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Notification> messageCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(messagingTemplate, times(2)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        List<String> destinations = destinationCaptor.getAllValues();
        assertTrue(destinations.contains("/topic/notifications/" + testUser1.getId()));
        assertTrue(destinations.contains("/topic/notifications/" + testUser2.getId()));

        List<Notification> sentNotifications = messageCaptor.getAllValues();
        assertEquals(2, sentNotifications.size());
        for (Notification notification : sentNotifications) {
            assertEquals("Event Reminder: Spring Conference 2025", notification.getTitle());
            assertTrue(notification.getMessage().contains("Spring Conference 2025"));
        }
    }

    @Test
    void sendNotification_ShouldSendCorrectWebSocketMessage() {
        // Arrange
        String userId = "123456";
        String title = "Test Title";
        String message = "Test Message";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        // Act
        notificationService.sendNotification(userId, title, message);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NotificationDTO> messageCaptor = ArgumentCaptor.forClass(NotificationDTO.class);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        assertEquals("/topic/notifications/" + userId, destinationCaptor.getValue());
        NotificationDTO capturedDto = messageCaptor.getValue();
        assertEquals(title, capturedDto.getTitle());
        assertEquals(message, capturedDto.getMessage());
        assertEquals("John Doe", capturedDto.getRecipientName());
        assertFalse(capturedDto.isRead());
    }
}
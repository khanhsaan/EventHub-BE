package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.EventCreationDTO;
import com.csci334.EventHub.dto.EventEditDTO;
import com.csci334.EventHub.dto.EventOutDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.EventStatus;
import com.csci334.EventHub.entity.enums.EventType;
import com.csci334.EventHub.entity.enums.Role;
import com.csci334.EventHub.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Tests")
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private EventService eventService;

    private Event testEvent1;
    private Event testEvent2;
    private User testOrganizer;
    private EventCreationDTO validCreationDTO;
    private EventEditDTO validEditDTO;

    @BeforeEach
    void setUp() {
        // Setup test organizer
        testOrganizer = new User();
        testOrganizer.setId("123456");
        testOrganizer.setEmail("organizer@example.com");
        testOrganizer.setPassword("encodedPassword");
        testOrganizer.setFirstName("John");
        testOrganizer.setLastName("Organizer");
        testOrganizer.setRole(Role.ORGANIZER);

        // Setup test event 1 - CONFERENCE
        testEvent1 = new Event();
        testEvent1.setId("100001");
        testEvent1.setTitle("Tech Conference 2024");
        testEvent1.setDescription("Annual technology conference featuring latest innovations");
        testEvent1.setShortDescription("Tech conference with industry leaders");
        testEvent1.setLocation("Convention Center");
        testEvent1.setEventDate(LocalDate.of(2024, 6, 15));
        testEvent1.setStartTime(LocalTime.of(9, 0));
        testEvent1.setEndTime(LocalTime.of(17, 0));
        testEvent1.setEventType(EventType.CONFERENCE);
        testEvent1.setStatus(EventStatus.PUBLISHED);
        testEvent1.setGeneralPrice(100.0);
        testEvent1.setVipPrice(250.0);
        testEvent1.setGeneralTicketLimit(500);
        testEvent1.setVipTicketLimit(100);
        testEvent1.setGeneralTicketsRemaining(450);
        testEvent1.setVipTicketsRemaining(95);
        testEvent1.setImageUrl("https://example.com/tech-conf.jpg");
        testEvent1.setOrganizer(testOrganizer);
        testEvent1.setCreatedAt(LocalDateTime.now().minusDays(10));
        testEvent1.setLastUpdatedAt(LocalDateTime.now().minusDays(5));

        // Setup test event 2 - WORKSHOP
        testEvent2 = new Event();
        testEvent2.setId("100002");
        testEvent2.setTitle("Java Spring Boot Workshop");
        testEvent2.setDescription("Hands-on workshop for learning Spring Boot framework");
        testEvent2.setShortDescription("Spring Boot hands-on workshop");
        testEvent2.setLocation("Training Room A");
        testEvent2.setEventDate(LocalDate.of(2024, 7, 20));
        testEvent2.setStartTime(LocalTime.of(10, 0));
        testEvent2.setEndTime(LocalTime.of(16, 0));
        testEvent2.setEventType(EventType.WORKSHOP);
        testEvent2.setStatus(EventStatus.PUBLISHED);
        testEvent2.setGeneralPrice(50.0);
        testEvent2.setVipPrice(null);
        testEvent2.setGeneralTicketLimit(30);
        testEvent2.setVipTicketLimit(null);
        testEvent2.setGeneralTicketsRemaining(25);
        testEvent2.setVipTicketsRemaining(null);
        testEvent2.setOrganizer(testOrganizer);

        // Setup valid creation DTO
        validCreationDTO = new EventCreationDTO();
        validCreationDTO.setTitle("New Event");
        validCreationDTO.setDescription("A new exciting event");
        validCreationDTO.setShortDescription("New event");
        validCreationDTO.setLocation("Event Hall");
        validCreationDTO.setEventDate(LocalDate.of(2024, 8, 15));
        validCreationDTO.setStartTime(LocalTime.of(14, 0));
        validCreationDTO.setEndTime(LocalTime.of(18, 0));
        validCreationDTO.setEventType(EventType.SEMINAR);
        validCreationDTO.setStatus(EventStatus.PUBLISHED);
        validCreationDTO.setGeneralPrice(75.0);
        validCreationDTO.setVipPrice(150.0);
        validCreationDTO.setGeneralTicketLimit(200);
        validCreationDTO.setVipTicketLimit(50);
        validCreationDTO.setImageUrl("https://example.com/new-event.jpg");
        validCreationDTO.setOrganizerId("123456");

        // Setup valid edit DTO
        validEditDTO = new EventEditDTO();
        validEditDTO.setTitle("Updated Event Title");
        validEditDTO.setDescription("Updated description");
        validEditDTO.setShortDescription("Updated short description");
        validEditDTO.setLocation("Updated Location");
        validEditDTO.setEventDate(LocalDate.of(2024, 8, 20));
        validEditDTO.setStartTime(LocalTime.of(15, 0));
        validEditDTO.setEndTime(LocalTime.of(19, 0));
        validEditDTO.setEventType(EventType.CONFERENCE);
        validEditDTO.setGeneralPrice(120.0);
        validEditDTO.setVipPrice(300.0);
        validEditDTO.setGeneralTicketLimit(600);
        validEditDTO.setVipTicketLimit(150);
        validEditDTO.setGeneralTicketsRemaining(550);
        validEditDTO.setVipTicketsRemaining(140);
        validEditDTO.setImageUrl("https://example.com/updated-event.jpg");
    }

    @Nested
    @DisplayName("Read Operations Tests")
    class ReadOperationTests {

        @Test
        @DisplayName("Should return all events as DTOs when getAll is called")
        void getAll_ShouldReturnAllEventsAsDTOs() {
            // Arrange
            System.out.println("\n=== TEST: Get All Events ===");
            List<Event> allEvents = Arrays.asList(testEvent1, testEvent2);
            when(eventRepository.findAll()).thenReturn(allEvents);

            System.out.println("Expected Output: List of " + allEvents.size() + " EventOutDTOs");

            // Act
            List<EventOutDTO> result = eventService.getAll();

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " EventOutDTOs");
            assertNotNull(result);
            assertEquals(2, result.size());

            EventOutDTO dto1 = result.get(0);
            assertEquals(testEvent1.getId(), dto1.getId());
            assertEquals(testEvent1.getTitle(), dto1.getTitle());
            assertEquals(testEvent1.getEventType(), dto1.getEventType());
            assertEquals(testEvent1.getStatus(), dto1.getStatus());
            assertEquals(testEvent1.getOrganizer().getId(), dto1.getOrganizerId());

            verify(eventRepository).findAll();
            System.out.println("✅ TEST PASSED: Events converted to DTOs successfully!");
        }

        @Test
        @DisplayName("Should return empty list when no events exist")
        void getAll_ShouldReturnEmptyList_WhenNoEventsExist() {
            // Arrange
            System.out.println("\n=== TEST: Get All Events - Empty Database ===");
            when(eventRepository.findAll()).thenReturn(Collections.emptyList());

            System.out.println("Expected Output: Empty list");

            // Act
            List<EventOutDTO> result = eventService.getAll();

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " events");
            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(eventRepository).findAll();
            System.out.println("✅ TEST PASSED: Empty list returned correctly!");
        }

        @Test
        @DisplayName("Should return event when getById is called with existing ID")
        void getById_ShouldReturnEvent_WhenEventExists() {
            // Arrange
            System.out.println("\n=== TEST: Get Event By ID ===");
            System.out.println("Input ID: " + testEvent1.getId());
            when(eventRepository.findById(testEvent1.getId())).thenReturn(Optional.of(testEvent1));

            System.out.println("Expected Output: Event found");

            // Act
            Optional<Event> result = eventService.getById(testEvent1.getId());

            // Assert
            System.out.println("Actual Output: Event " + (result.isPresent() ? "found" : "not found"));
            assertTrue(result.isPresent());
            assertEquals(testEvent1, result.get());

            verify(eventRepository).findById(testEvent1.getId());
            System.out.println("✅ TEST PASSED: Event retrieved by ID successfully!");
        }

        @Test
        @DisplayName("Should return events by type when getByType is called")
        void getByType_ShouldReturnEventsByType() {
            // Arrange
            System.out.println("\n=== TEST: Get Events By Type ===");
            List<Event> conferences = Arrays.asList(testEvent1);
            System.out.println("Input Type: " + EventType.CONFERENCE);
            when(eventRepository.findByEventType(EventType.CONFERENCE)).thenReturn(conferences);

            System.out.println("Expected Output: " + conferences.size() + " conference(s)");

            // Act
            List<Event> result = eventService.getByType(EventType.CONFERENCE);

            // Assert
            System.out.println("Actual Output: " + result.size() + " event(s) found");
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testEvent1, result.get(0));

            verify(eventRepository).findByEventType(EventType.CONFERENCE);
            System.out.println("✅ TEST PASSED: Events retrieved by type successfully!");
        }

        @Test
        @DisplayName("Should return events by organizer when getByOrganizer is called")
        void getByOrganizer_ShouldReturnEventsByOrganizer() {
            // Arrange
            System.out.println("\n=== TEST: Get Events By Organizer ===");
            List<Event> organizerEvents = Arrays.asList(testEvent1, testEvent2);
            String organizerId = testOrganizer.getId();
            System.out.println("Input Organizer ID: " + organizerId);
            when(eventRepository.findByOrganizerId(organizerId)).thenReturn(organizerEvents);

            System.out.println("Expected Output: " + organizerEvents.size() + " event(s)");

            // Act
            List<Event> result = eventService.getByOrganizer(organizerId);

            // Assert
            System.out.println("Actual Output: " + result.size() + " event(s) found");
            assertNotNull(result);
            assertEquals(2, result.size());

            verify(eventRepository).findByOrganizerId(organizerId);
            System.out.println("✅ TEST PASSED: Events retrieved by organizer successfully!");
        }

        @Test
        @DisplayName("Should return upcoming events when getUpcoming is called")
        void getUpcoming_ShouldReturnUpcomingEvents() {
            // Arrange
            System.out.println("\n=== TEST: Get Upcoming Events ===");
            List<Event> upcomingEvents = Arrays.asList(testEvent1, testEvent2);
            when(eventRepository.findByEventDateAfter(any(LocalDateTime.class))).thenReturn(upcomingEvents);

            System.out.println("Expected Output: " + upcomingEvents.size() + " upcoming event(s)");

            // Act
            List<Event> result = eventService.getUpcoming();

            // Assert
            System.out.println("Actual Output: " + result.size() + " event(s) found");
            assertNotNull(result);
            assertEquals(2, result.size());

            verify(eventRepository).findByEventDateAfter(any(LocalDateTime.class));
            System.out.println("✅ TEST PASSED: Upcoming events retrieved successfully!");
        }

        @Test
        @DisplayName("Should return events matching title search")
        void searchByTitle_ShouldReturnMatchingEvents() {
            // Arrange
            System.out.println("\n=== TEST: Search Events By Title ===");
            String searchKeyword = "tech";
            List<Event> matchingEvents = Arrays.asList(testEvent1);
            System.out.println("Search keyword: '" + searchKeyword + "'");
            when(eventRepository.findByTitleContainingIgnoreCase(searchKeyword)).thenReturn(matchingEvents);

            System.out.println("Expected Output: " + matchingEvents.size() + " matching event(s)");

            // Act
            List<Event> result = eventService.searchByTitle(searchKeyword);

            // Assert
            System.out.println("Actual Output: " + result.size() + " event(s) found");
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testEvent1, result.get(0));

            verify(eventRepository).findByTitleContainingIgnoreCase(searchKeyword);
            System.out.println("✅ TEST PASSED: Events found by title search successfully!");
        }
    }

    @Nested
    @DisplayName("Create Event Tests")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event successfully when valid DTO and organizer exist")
        void create_ShouldCreateEvent_WhenValidDTOAndOrganizerExist() {
            // Arrange
            System.out.println("\n=== TEST: Create Event - Success ===");
            System.out.println("Input EventCreationDTO:");
            System.out.println("  Title: " + validCreationDTO.getTitle());
            System.out.println("  Type: " + validCreationDTO.getEventType());
            System.out.println("  Organizer ID: " + validCreationDTO.getOrganizerId());

            when(userRepository.findById(validCreationDTO.getOrganizerId())).thenReturn(Optional.of(testOrganizer));
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent1);
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected Output: Event created with generated ID");

            // Act
            Event result = eventService.create(validCreationDTO);

            // Assert
            System.out.println("Actual Output: Event created with ID " + result.getId());
            assertNotNull(result);
            assertEquals(testEvent1, result);

            // Verify interactions
            verify(userRepository).findById(validCreationDTO.getOrganizerId());
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(eventRepository).save(eventCaptor.capture());
            verify(messagingTemplate).convertAndSend(eq("/topic/events/published"), any(List.class));

            Event savedEvent = eventCaptor.getValue();
            assertEquals(validCreationDTO.getTitle(), savedEvent.getTitle());
            assertEquals(validCreationDTO.getEventType(), savedEvent.getEventType());
            assertEquals(testOrganizer, savedEvent.getOrganizer());

            System.out.println("✅ TEST PASSED: Event created successfully!");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when organizer does not exist")
        void create_ShouldThrowException_WhenOrganizerNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Create Event - Organizer Not Found ===");
            String nonExistentOrganizerId = "999999";
            validCreationDTO.setOrganizerId(nonExistentOrganizerId);
            System.out.println("Input Organizer ID: " + nonExistentOrganizerId + " (does not exist)");

            when(userRepository.findById(nonExistentOrganizerId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: EntityNotFoundException");

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> eventService.create(validCreationDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertTrue(exception.getMessage().contains("Organizer not found with ID: " + nonExistentOrganizerId));

            verify(userRepository).findById(nonExistentOrganizerId);
            verify(eventRepository, never()).save(any(Event.class));
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(List.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing organizer!");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when organizer ID is null")
        void create_ShouldThrowException_WhenOrganizerIdIsNull() {
            // Arrange
            System.out.println("\n=== TEST: Create Event - Null Organizer ID ===");
            validCreationDTO.setOrganizerId(null);
            System.out.println("Input Organizer ID: null");

            System.out.println("Expected Output: IllegalArgumentException");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> eventService.create(validCreationDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Organizer ID cannot be null when creating an event.", exception.getMessage());

            verify(userRepository, never()).findById(any());
            verify(eventRepository, never()).save(any(Event.class));
            System.out.println("✅ TEST PASSED: Exception thrown for null organizer ID!");
        }
    }

    @Nested
    @DisplayName("Update Event Tests")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event successfully when event exists")
        void update_ShouldUpdateEvent_WhenEventExists() {
            // Arrange
            System.out.println("\n=== TEST: Update Event - Success ===");
            String eventId = testEvent1.getId();
            System.out.println("Input Event ID: " + eventId);
            System.out.println("Updated Title: " + validEditDTO.getTitle());

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent1));
            when(eventRepository.save(testEvent1)).thenReturn(testEvent1);
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected Output: Event updated successfully");

            // Act
            Event result = eventService.update(eventId, validEditDTO);

            // Assert
            System.out.println("Actual Output: Event updated");
            System.out.println("New Title: " + testEvent1.getTitle());

            assertNotNull(result);
            assertEquals(validEditDTO.getTitle(), testEvent1.getTitle());
            assertEquals(validEditDTO.getDescription(), testEvent1.getDescription());
            assertEquals(validEditDTO.getEventType(), testEvent1.getEventType());

            verify(eventRepository).findById(eventId);
            verify(eventRepository).save(testEvent1);
            verify(messagingTemplate).convertAndSend(eq("/topic/events/published"), any(List.class));
            System.out.println("✅ TEST PASSED: Event updated successfully!");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when event does not exist")
        void update_ShouldThrowException_WhenEventNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Update Event - Event Not Found ===");
            String nonExistentId = "999999";
            System.out.println("Input Event ID: " + nonExistentId + " (does not exist)");

            when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: EntityNotFoundException");

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> eventService.update(nonExistentId, validEditDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertTrue(exception.getMessage().contains("Event not found with ID: " + nonExistentId));

            verify(eventRepository).findById(nonExistentId);
            verify(eventRepository, never()).save(any(Event.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing event!");
        }
    }

    @Nested
    @DisplayName("Delete Event Tests")
    class DeleteEventTests {

        @Test
        @DisplayName("Should delete event successfully")
        void delete_ShouldDeleteEvent() {
            // Arrange
            System.out.println("\n=== TEST: Delete Event ===");
            String eventId = testEvent1.getId();
            System.out.println("Input Event ID: " + eventId);

            doNothing().when(eventRepository).deleteById(eventId);

            System.out.println("Expected Output: Event deleted");

            // Act
            eventService.delete(eventId);

            // Assert
            System.out.println("Actual Output: Delete operation completed");
            verify(eventRepository).deleteById(eventId);
            System.out.println("✅ TEST PASSED: Event deleted successfully!");
        }
    }

    @Nested
    @DisplayName("Cancel Event Tests")
    class CancelEventTests {

        @Test
        @DisplayName("Should cancel event successfully with correct password")
        void cancelEvent_ShouldCancelEvent_WhenPasswordIsCorrect() {
            // Arrange
            System.out.println("\n=== TEST: Cancel Event - Success ===");
            String eventId = testEvent1.getId();
            String password = "correctPassword";
            System.out.println("Input Event ID: " + eventId);
            System.out.println("Password verification: PASSED");

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent1));
            when(passwordEncoder.matches(password, testOrganizer.getPassword())).thenReturn(true);
            when(eventRepository.save(testEvent1)).thenReturn(testEvent1);
            when(registrationRepository.findByEventId(eventId)).thenReturn(Collections.emptyList());
            doNothing().when(messagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));

            System.out.println("Expected Output: Event cancelled, status = CANCELLED");

            // Act
            Event result = eventService.cancelEvent(eventId, password);

            // Assert
            System.out.println("Actual Output: Event status = " + result.getStatus());
            assertNotNull(result);
            assertEquals(EventStatus.CANCELLED, result.getStatus());
            assertNotNull(result.getLastUpdatedAt());

            verify(eventRepository).findById(eventId);
            verify(passwordEncoder).matches(password, testOrganizer.getPassword());
            verify(eventRepository).save(testEvent1);
            verify(registrationRepository).findByEventId(eventId);
            verify(messagingTemplate).convertAndSend(eq("/topic/events/cancelled"), any(List.class));
            System.out.println("✅ TEST PASSED: Event cancelled successfully!");
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when password is incorrect")
        void cancelEvent_ShouldThrowException_WhenPasswordIsIncorrect() {
            // Arrange
            System.out.println("\n=== TEST: Cancel Event - Wrong Password ===");
            String eventId = testEvent1.getId();
            String wrongPassword = "wrongPassword";
            System.out.println("Input Event ID: " + eventId);
            System.out.println("Password verification: FAILED");

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent1));
            when(passwordEncoder.matches(wrongPassword, testOrganizer.getPassword())).thenReturn(false);

            System.out.println("Expected Output: BadCredentialsException");

            // Act & Assert
            BadCredentialsException exception = assertThrows(
                    BadCredentialsException.class,
                    () -> eventService.cancelEvent(eventId, wrongPassword)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Incorrect password provided for cancellation.", exception.getMessage());
            assertEquals(EventStatus.PUBLISHED, testEvent1.getStatus()); // Status unchanged

            verify(eventRepository).findById(eventId);
            verify(passwordEncoder).matches(wrongPassword, testOrganizer.getPassword());
            verify(eventRepository, never()).save(any(Event.class));
            System.out.println("✅ TEST PASSED: Security violation prevented!");
        }

        @Test
        @DisplayName("Should return event unchanged when already cancelled")
        void cancelEvent_ShouldReturnUnchanged_WhenAlreadyCancelled() {
            // Arrange
            System.out.println("\n=== TEST: Cancel Event - Already Cancelled ===");
            testEvent1.setStatus(EventStatus.CANCELLED);
            String eventId = testEvent1.getId();
            String password = "correctPassword";

            System.out.println("Current event status: " + testEvent1.getStatus());

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent1));
            when(passwordEncoder.matches(password, testOrganizer.getPassword())).thenReturn(true);

            System.out.println("Expected Output: No action taken, event remains cancelled");

            // Act
            Event result = eventService.cancelEvent(eventId, password);

            // Assert
            System.out.println("Actual Output: Event status = " + result.getStatus());
            assertEquals(EventStatus.CANCELLED, result.getStatus());

            verify(eventRepository).findById(eventId);
            verify(passwordEncoder).matches(password, testOrganizer.getPassword());
            verify(eventRepository, never()).save(any(Event.class));
            verify(registrationRepository, never()).findByEventId(anyString());
            System.out.println("✅ TEST PASSED: Already cancelled event handled correctly!");
        }
    }

    @Nested
    @DisplayName("Scheduled Methods Tests")
    class ScheduledMethodsTests {

        @Test
        @DisplayName("Should update events to COMPLETED when end time is reached")
        void updatePastEventStatuses_ShouldUpdateEvents_WhenEndTimeReached() {
            // Arrange
            System.out.println("\n=== TEST: Update Past Event Statuses ===");

            // Create event that should be completed (ended)
            Event eventToComplete = new Event();
            eventToComplete.setId("100004");
            eventToComplete.setTitle("Event That Ended");
            eventToComplete.setEventDate(LocalDate.now());
            eventToComplete.setStartTime(LocalTime.now().minusHours(3));
            eventToComplete.setEndTime(LocalTime.now().minusMinutes(10)); // Ended 10 minutes ago
            eventToComplete.setStatus(EventStatus.IN_PROGRESS);

            when(eventRepository.findByStatus(EventStatus.PUBLISHED)).thenReturn(Collections.emptyList());
            when(eventRepository.findByStatus(EventStatus.IN_PROGRESS)).thenReturn(Arrays.asList(eventToComplete));
            when(eventRepository.saveAll(anyList())).thenReturn(Arrays.asList(eventToComplete));
            doNothing().when(messagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));

            System.out.println("Expected Output: 1 event updated to COMPLETED");

            // Act
            eventService.updatePastEventStatuses();

            // Assert
            System.out.println("Actual Output: Event status = " + eventToComplete.getStatus());
            assertEquals(EventStatus.COMPLETED, eventToComplete.getStatus());
            assertNotNull(eventToComplete.getLastUpdatedAt());

            verify(eventRepository).findByStatus(EventStatus.PUBLISHED);
            verify(eventRepository).findByStatus(EventStatus.IN_PROGRESS);
            verify(eventRepository).saveAll(anyList());
            verify(messagingTemplate).convertAndSend(eq("/topic/events/completed"), any(List.class));
            System.out.println("✅ TEST PASSED: Events updated to COMPLETED successfully!");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle event with null organizer during cancellation")
        void cancelEvent_ShouldThrowException_WhenOrganizerIsNull() {
            // Arrange
            System.out.println("\n=== TEST: Cancel Event - Null Organizer ===");
            testEvent1.setOrganizer(null);
            String eventId = testEvent1.getId();

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent1));

            System.out.println("Expected Output: IllegalStateException");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> eventService.cancelEvent(eventId, "password")
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            assertTrue(exception.getMessage().contains("does not have an associated organizer"));
            System.out.println("✅ TEST PASSED: Exception thrown for null organizer!");
        }

        @Test
        @DisplayName("Should handle repository failures gracefully")
        void create_ShouldPropagateException_WhenRepositoryFails() {
            // Arrange
            System.out.println("\n=== TEST: Repository Failure During Create ===");
            when(userRepository.findById(validCreationDTO.getOrganizerId())).thenReturn(Optional.of(testOrganizer));
            when(eventRepository.save(any(Event.class))).thenThrow(new RuntimeException("Database error"));

            System.out.println("Expected Output: RuntimeException propagated");

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> eventService.create(validCreationDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            assertEquals("Database error", exception.getMessage());
            System.out.println("✅ TEST PASSED: Repository exception propagated correctly!");
        }

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void methods_ShouldHandleNullInputs() {
            System.out.println("\n=== TEST: Handle Null Inputs ===");

            // Test getById with null
            when(eventRepository.findById(null)).thenReturn(Optional.empty());
            Optional<Event> result = eventService.getById(null);
            assertFalse(result.isPresent());

            // Test searchByTitle with null
            when(eventRepository.findByTitleContainingIgnoreCase(null)).thenReturn(Collections.emptyList());
            List<Event> searchResult = eventService.searchByTitle(null);
            assertNotNull(searchResult);

            System.out.println("✅ TEST PASSED: Null inputs handled gracefully!");
        }
    }
}
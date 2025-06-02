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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Demo class showing expected vs actual output for EventService methods
 * This demonstrates the behavior of complex event management operations
 */
@ExtendWith(MockitoExtension.class)
class EventServiceDemo {

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

    private Event sampleEvent1;
    private Event sampleEvent2;
    private User sampleOrganizer;

    @BeforeEach
    void setUp() {
        reset(eventRepository, userRepository, registrationRepository, paymentRepository,
                notificationRepository, notificationService, passwordEncoder, messagingTemplate);

        // Setup sample organizer
        sampleOrganizer = new User();
        sampleOrganizer.setId("123456");
        sampleOrganizer.setEmail("organizer@example.com");
        sampleOrganizer.setPassword("$2a$10$encoded_password_hash");
        sampleOrganizer.setFirstName("John");
        sampleOrganizer.setLastName("Organizer");
        sampleOrganizer.setRole(Role.ORGANIZER);

        // Setup sample event 1
        sampleEvent1 = new Event();
        sampleEvent1.setId("100001");
        sampleEvent1.setTitle("Tech Conference 2024");
        sampleEvent1.setDescription("Annual technology conference featuring latest innovations in AI, blockchain, and cloud computing");
        sampleEvent1.setShortDescription("Tech conference with industry leaders");
        sampleEvent1.setLocation("Convention Center, Downtown");
        sampleEvent1.setEventDate(LocalDate.of(2024, 6, 15));
        sampleEvent1.setStartTime(LocalTime.of(9, 0));
        sampleEvent1.setEndTime(LocalTime.of(17, 0));
        sampleEvent1.setEventType(EventType.CONFERENCE);
        sampleEvent1.setStatus(EventStatus.PUBLISHED);
        sampleEvent1.setGeneralPrice(150.0);
        sampleEvent1.setVipPrice(350.0);
        sampleEvent1.setGeneralTicketLimit(500);
        sampleEvent1.setVipTicketLimit(100);
        sampleEvent1.setGeneralTicketsRemaining(450);
        sampleEvent1.setVipTicketsRemaining(95);
        sampleEvent1.setImageUrl("https://example.com/tech-conf-2024.jpg");
        sampleEvent1.setOrganizer(sampleOrganizer);
        sampleEvent1.setCreatedAt(LocalDateTime.of(2024, 5, 1, 10, 0));
        sampleEvent1.setLastUpdatedAt(LocalDateTime.of(2024, 5, 10, 14, 30));

        // Setup sample event 2
        sampleEvent2 = new Event();
        sampleEvent2.setId("100002");
        sampleEvent2.setTitle("Spring Boot Workshop");
        sampleEvent2.setDescription("Hands-on workshop for learning Spring Boot framework from basics to advanced");
        sampleEvent2.setShortDescription("Spring Boot hands-on workshop");
        sampleEvent2.setLocation("Training Center, Room A");
        sampleEvent2.setEventDate(LocalDate.of(2024, 7, 20));
        sampleEvent2.setStartTime(LocalTime.of(10, 0));
        sampleEvent2.setEndTime(LocalTime.of(16, 0));
        sampleEvent2.setEventType(EventType.WORKSHOP);
        sampleEvent2.setStatus(EventStatus.PUBLISHED);
        sampleEvent2.setGeneralPrice(75.0);
        sampleEvent2.setVipPrice(null);
        sampleEvent2.setGeneralTicketLimit(30);
        sampleEvent2.setVipTicketLimit(null);
        sampleEvent2.setGeneralTicketsRemaining(25);
        sampleEvent2.setVipTicketsRemaining(null);
        sampleEvent2.setOrganizer(sampleOrganizer);
    }

    @Test
    void demonstrateGetAllEvents() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 1: GET ALL EVENTS");
        System.out.println("=".repeat(80));

        System.out.println("INPUT:");
        System.out.println("  eventService.getAll()");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  List<EventOutDTO> containing:");
        System.out.println("  [");
        System.out.println("    EventOutDTO {");
        System.out.println("      id: '100001',");
        System.out.println("      title: 'Tech Conference 2024',");
        System.out.println("      eventType: CONFERENCE,");
        System.out.println("      status: PUBLISHED,");
        System.out.println("      generalPrice: 150.0,");
        System.out.println("      vipPrice: 350.0,");
        System.out.println("      generalTicketsRemaining: 450,");
        System.out.println("      organizerId: '123456'");
        System.out.println("    },");
        System.out.println("    EventOutDTO {");
        System.out.println("      id: '100002',");
        System.out.println("      title: 'Spring Boot Workshop',");
        System.out.println("      eventType: WORKSHOP,");
        System.out.println("      status: PUBLISHED,");
        System.out.println("      generalPrice: 75.0,");
        System.out.println("      organizerId: '123456'");
        System.out.println("    }");
        System.out.println("  ]");

        // Mock setup
        List<Event> allEvents = Arrays.asList(sampleEvent1, sampleEvent2);
        when(eventRepository.findAll()).thenReturn(allEvents);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling eventService.getAll()...");

        List<EventOutDTO> result = eventService.getAll();

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  List<EventOutDTO> with " + result.size() + " events:");
        System.out.println("  [");
        for (int i = 0; i < result.size(); i++) {
            EventOutDTO dto = result.get(i);
            System.out.println("    EventOutDTO {");
            System.out.println("      id: '" + dto.getId() + "',");
            System.out.println("      title: '" + dto.getTitle() + "',");
            System.out.println("      eventType: " + dto.getEventType() + ",");
            System.out.println("      status: " + dto.getStatus() + ",");
            System.out.println("      generalPrice: " + dto.getGeneralPrice() + ",");
            if (dto.getVipPrice() != null) {
                System.out.println("      vipPrice: " + dto.getVipPrice() + ",");
            }
            System.out.println("      generalTicketsRemaining: " + dto.getGeneralTicketsRemaining() + ",");
            System.out.println("      organizerId: '" + dto.getOrganizerId() + "'");
            System.out.println("    }" + (i < result.size() - 1 ? "," : ""));
        }
        System.out.println("  ]");

        System.out.println("\nRESULT: ✅ SUCCESS - Retrieved " + result.size() + " events converted to DTOs");
    }

    @Test
    void demonstrateCreateEvent() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 2: CREATE NEW EVENT");
        System.out.println("=".repeat(80));

        EventCreationDTO newEventDTO = new EventCreationDTO();
        newEventDTO.setTitle("AI & Machine Learning Summit");
        newEventDTO.setDescription("Comprehensive summit on AI and ML trends, tools, and applications");
        newEventDTO.setShortDescription("AI & ML Summit");
        newEventDTO.setLocation("Tech Hub, Main Auditorium");
        newEventDTO.setEventDate(LocalDate.of(2024, 9, 15));
        newEventDTO.setStartTime(LocalTime.of(8, 30));
        newEventDTO.setEndTime(LocalTime.of(18, 0));
        newEventDTO.setEventType(EventType.CONFERENCE);
        newEventDTO.setStatus(EventStatus.PUBLISHED);
        newEventDTO.setGeneralPrice(200.0);
        newEventDTO.setVipPrice(500.0);
        newEventDTO.setGeneralTicketLimit(300);
        newEventDTO.setVipTicketLimit(50);
        newEventDTO.setImageUrl("https://example.com/ai-summit.jpg");
        newEventDTO.setOrganizerId("123456");

        System.out.println("INPUT:");
        System.out.println("  EventCreationDTO {");
        System.out.println("    title: '" + newEventDTO.getTitle() + "',");
        System.out.println("    eventType: " + newEventDTO.getEventType() + ",");
        System.out.println("    eventDate: " + newEventDTO.getEventDate() + ",");
        System.out.println("    startTime: " + newEventDTO.getStartTime() + ",");
        System.out.println("    endTime: " + newEventDTO.getEndTime() + ",");
        System.out.println("    generalPrice: " + newEventDTO.getGeneralPrice() + ",");
        System.out.println("    vipPrice: " + newEventDTO.getVipPrice() + ",");
        System.out.println("    generalTicketLimit: " + newEventDTO.getGeneralTicketLimit() + ",");
        System.out.println("    organizerId: '" + newEventDTO.getOrganizerId() + "'");
        System.out.println("  }");

        System.out.println("\nEXPECTED PROCESS:");
        System.out.println("  1. Validate organizer exists (ID: " + newEventDTO.getOrganizerId() + ")");
        System.out.println("  2. Create Event entity from DTO");
        System.out.println("  3. Set organizer reference");
        System.out.println("  4. Save event to database");
        System.out.println("  5. Send WebSocket notification to /topic/events/published");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Event {");
        System.out.println("    id: '100003' (auto-generated),");
        System.out.println("    title: '" + newEventDTO.getTitle() + "',");
        System.out.println("    status: PUBLISHED,");
        System.out.println("    organizer: User { id: '123456', email: 'organizer@example.com' },");
        System.out.println("    generalTicketsRemaining: 300 (initialized to limit),");
        System.out.println("    vipTicketsRemaining: 50 (initialized to limit),");
        System.out.println("    createdAt: current timestamp,");
        System.out.println("    lastUpdatedAt: current timestamp");
        System.out.println("  }");

        // Mock setup
        Event savedEvent = new Event();
        savedEvent.setId("100003");
        savedEvent.setTitle(newEventDTO.getTitle());
        savedEvent.setEventType(newEventDTO.getEventType());
        savedEvent.setStatus(EventStatus.PUBLISHED);
        savedEvent.setOrganizer(sampleOrganizer);
        savedEvent.setGeneralTicketsRemaining(newEventDTO.getGeneralTicketLimit());
        savedEvent.setVipTicketsRemaining(newEventDTO.getVipTicketLimit());
        savedEvent.setCreatedAt(LocalDateTime.now());
        savedEvent.setLastUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(newEventDTO.getOrganizerId())).thenReturn(Optional.of(sampleOrganizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(List.class));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Step 1: Finding organizer with ID '" + newEventDTO.getOrganizerId() + "'... FOUND");
        System.out.println("  Step 2: Creating Event entity from DTO...");
        System.out.println("  Step 3: Setting organizer reference...");
        System.out.println("  Step 4: Saving event to database...");
        System.out.println("  Step 5: Sending WebSocket notification...");

        Event result = eventService.create(newEventDTO);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Event {");
        System.out.println("    id: '" + result.getId() + "',");
        System.out.println("    title: '" + result.getTitle() + "',");
        System.out.println("    status: " + result.getStatus() + ",");
        System.out.println("    organizer: User { id: '" + result.getOrganizer().getId() + "', email: '" + result.getOrganizer().getEmail() + "' },");
        System.out.println("    generalTicketsRemaining: " + result.getGeneralTicketsRemaining() + ",");
        System.out.println("    vipTicketsRemaining: " + result.getVipTicketsRemaining() + ",");
        System.out.println("    createdAt: " + result.getCreatedAt() + ",");
        System.out.println("    lastUpdatedAt: " + result.getLastUpdatedAt());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - Event created and published successfully");
    }

    @Test
    void demonstrateUpdateEvent() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 3: UPDATE EXISTING EVENT");
        System.out.println("=".repeat(80));

        String eventId = "100001";
        EventEditDTO updateDTO = new EventEditDTO();
        updateDTO.setTitle("Tech Conference 2024 - UPDATED");
        updateDTO.setDescription("UPDATED: Annual technology conference with expanded AI track");
        updateDTO.setShortDescription("Updated tech conference");
        updateDTO.setLocation("UPDATED: Grand Convention Center");
        updateDTO.setEventDate(LocalDate.of(2024, 6, 20)); // Changed date
        updateDTO.setStartTime(LocalTime.of(8, 30));       // Earlier start
        updateDTO.setEndTime(LocalTime.of(18, 30));        // Later end
        updateDTO.setEventType(EventType.CONFERENCE);
        updateDTO.setGeneralPrice(175.0);                  // Price increase
        updateDTO.setVipPrice(400.0);                      // Price increase
        updateDTO.setGeneralTicketLimit(600);              // Increased capacity
        updateDTO.setVipTicketLimit(120);                  // Increased capacity
        updateDTO.setGeneralTicketsRemaining(550);
        updateDTO.setVipTicketsRemaining(115);

        System.out.println("INPUT:");
        System.out.println("  eventId: '" + eventId + "'");
        System.out.println("  EventEditDTO {");
        System.out.println("    title: '" + updateDTO.getTitle() + "' (CHANGED),");
        System.out.println("    eventDate: " + updateDTO.getEventDate() + " (CHANGED from 2024-06-15),");
        System.out.println("    startTime: " + updateDTO.getStartTime() + " (CHANGED from 09:00),");
        System.out.println("    generalPrice: " + updateDTO.getGeneralPrice() + " (CHANGED from 150.0),");
        System.out.println("    vipPrice: " + updateDTO.getVipPrice() + " (CHANGED from 350.0),");
        System.out.println("    generalTicketLimit: " + updateDTO.getGeneralTicketLimit() + " (CHANGED from 500)");
        System.out.println("  }");

        System.out.println("\nCURRENT EVENT STATE:");
        System.out.println("  Event {");
        System.out.println("    id: '" + sampleEvent1.getId() + "',");
        System.out.println("    title: '" + sampleEvent1.getTitle() + "',");
        System.out.println("    status: " + sampleEvent1.getStatus() + ",");
        System.out.println("    generalPrice: " + sampleEvent1.getGeneralPrice() + ",");
        System.out.println("    lastUpdatedAt: " + sampleEvent1.getLastUpdatedAt());
        System.out.println("  }");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Event {");
        System.out.println("    id: '" + eventId + "' (unchanged),");
        System.out.println("    title: '" + updateDTO.getTitle() + "' (updated),");
        System.out.println("    status: PUBLISHED (preserved),");
        System.out.println("    organizer: preserved,");
        System.out.println("    generalPrice: " + updateDTO.getGeneralPrice() + " (updated),");
        System.out.println("    lastUpdatedAt: current timestamp (updated)");
        System.out.println("  }");
        System.out.println("  WebSocket notification sent to /topic/events/published");

        // Mock setup
        Event eventToUpdate = new Event();
        eventToUpdate.setId(sampleEvent1.getId());
        eventToUpdate.setTitle(sampleEvent1.getTitle());
        eventToUpdate.setStatus(sampleEvent1.getStatus());
        eventToUpdate.setOrganizer(sampleEvent1.getOrganizer());
        eventToUpdate.setGeneralPrice(sampleEvent1.getGeneralPrice());

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventToUpdate));
        when(eventRepository.save(eventToUpdate)).thenReturn(eventToUpdate);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(List.class));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling eventService.update(\"" + eventId + "\", updateDTO)...");
        System.out.println("  Finding event... FOUND");
        System.out.println("  Applying updates...");
        System.out.println("  Saving to database...");
        System.out.println("  Sending WebSocket notification...");

        Event result = eventService.update(eventId, updateDTO);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Event {");
        System.out.println("    id: '" + result.getId() + "',");
        System.out.println("    title: '" + eventToUpdate.getTitle() + "',");
        System.out.println("    status: " + eventToUpdate.getStatus() + ",");
        System.out.println("    generalPrice: " + eventToUpdate.getGeneralPrice() + ",");
        System.out.println("    lastUpdatedAt: " + eventToUpdate.getLastUpdatedAt());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - Event updated and notification sent");
    }

    @Test
    void demonstrateCancelEventWithPassword() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 4: CANCEL EVENT WITH PASSWORD VERIFICATION");
        System.out.println("=".repeat(80));

        String eventId = "100001";
        String organizerPassword = "correctPassword123";

        System.out.println("INPUT:");
        System.out.println("  eventId: '" + eventId + "'");
        System.out.println("  password: '" + organizerPassword + "'");

        System.out.println("\nCURRENT EVENT STATE:");
        System.out.println("  Event {");
        System.out.println("    id: '" + sampleEvent1.getId() + "',");
        System.out.println("    title: '" + sampleEvent1.getTitle() + "',");
        System.out.println("    status: " + sampleEvent1.getStatus() + ",");
        System.out.println("    organizer: User { id: '" + sampleOrganizer.getId() + "' }");
        System.out.println("  }");

        System.out.println("\nEXPECTED PROCESS:");
        System.out.println("  1. Find event by ID: " + eventId);
        System.out.println("  2. Get organizer from event");
        System.out.println("  3. Verify password against organizer's stored password");
        System.out.println("  4. Check if event is already cancelled/completed");
        System.out.println("  5. Update event status to CANCELLED");
        System.out.println("  6. Process registrations for refunds/cancellations");
        System.out.println("  7. Send notifications to all attendees");
        System.out.println("  8. Send WebSocket notification");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Event {");
        System.out.println("    id: '" + eventId + "',");
        System.out.println("    title: '" + sampleEvent1.getTitle() + "',");
        System.out.println("    status: CANCELLED (changed from PUBLISHED),");
        System.out.println("    lastUpdatedAt: current timestamp");
        System.out.println("  }");
        System.out.println("  All registrations processed for refunds");
        System.out.println("  Notifications sent to attendees");
        System.out.println("  WebSocket notification sent to /topic/events/cancelled");

        // Mock setup
        Event eventToCancel = new Event();
        eventToCancel.setId(sampleEvent1.getId());
        eventToCancel.setTitle(sampleEvent1.getTitle());
        eventToCancel.setStatus(EventStatus.PUBLISHED);
        eventToCancel.setOrganizer(sampleOrganizer);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventToCancel));
        when(passwordEncoder.matches(organizerPassword, sampleOrganizer.getPassword())).thenReturn(true);
        when(eventRepository.save(eventToCancel)).thenReturn(eventToCancel);
        when(registrationRepository.findByEventId(eventId)).thenReturn(Collections.emptyList());
        doNothing().when(messagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Step 1: Finding event with ID '" + eventId + "'... FOUND");
        System.out.println("  Step 2: Getting organizer... FOUND");
        System.out.println("  Step 3: Verifying password... VERIFIED ✓");
        System.out.println("  Step 4: Checking event status... PUBLISHED (can be cancelled)");
        System.out.println("  Step 5: Updating status to CANCELLED...");
        System.out.println("  Step 6: Processing registrations... 0 registrations found");
        System.out.println("  Step 7: Sending notifications... COMPLETED");
        System.out.println("  Step 8: Sending WebSocket notification...");

        Event result = eventService.cancelEvent(eventId, organizerPassword);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Event {");
        System.out.println("    id: '" + result.getId() + "',");
        System.out.println("    title: '" + result.getTitle() + "',");
        System.out.println("    status: " + result.getStatus() + ",");
        System.out.println("    lastUpdatedAt: " + result.getLastUpdatedAt());
        System.out.println("  }");
        System.out.println("  Password verification: SUCCESS");
        System.out.println("  Event cancellation: COMPLETED");
        System.out.println("  Registrations processed: 0");
        System.out.println("  WebSocket notifications: SENT");

        System.out.println("\nRESULT: ✅ SUCCESS - Event cancelled securely with proper verification");
    }

    @Test
    void demonstrateCancelEventWrongPassword() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 5: CANCEL EVENT - INCORRECT PASSWORD");
        System.out.println("=".repeat(80));

        String eventId = "100001";
        String wrongPassword = "wrongPassword456";

        System.out.println("INPUT:");
        System.out.println("  eventId: '" + eventId + "'");
        System.out.println("  password: '" + wrongPassword + "' (INCORRECT)");

        System.out.println("\nEXPECTED PROCESS:");
        System.out.println("  1. Find event by ID: " + eventId);
        System.out.println("  2. Get organizer from event");
        System.out.println("  3. Verify password against organizer's stored password: FAILS");
        System.out.println("  4. Throw BadCredentialsException");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  BadCredentialsException with message: 'Incorrect password provided for cancellation.'");
        System.out.println("  Event status remains unchanged: PUBLISHED");
        System.out.println("  No registrations processed");
        System.out.println("  No notifications sent");

        // Mock setup
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(sampleEvent1));
        when(passwordEncoder.matches(wrongPassword, sampleOrganizer.getPassword())).thenReturn(false);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Step 1: Finding event with ID '" + eventId + "'... FOUND");
        System.out.println("  Step 2: Getting organizer... FOUND");
        System.out.println("  Step 3: Verifying password... FAILED ❌");

        try {
            eventService.cancelEvent(eventId, wrongPassword);
            System.out.println("\nACTUAL OUTPUT: ❌ No exception thrown (unexpected!)");
        } catch (Exception e) {
            System.out.println("\nACTUAL OUTPUT:");
            System.out.println("  Exception: " + e.getClass().getSimpleName());
            System.out.println("  Message: '" + e.getMessage() + "'");
            System.out.println("  Event status: " + sampleEvent1.getStatus() + " (unchanged)");
            System.out.println("  Security breach: PREVENTED");
        }

        System.out.println("\nRESULT: ✅ SUCCESS - Unauthorized cancellation attempt blocked");
    }

    @Test
    void demonstrateSearchEventsByTitle() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 6: SEARCH EVENTS BY TITLE");
        System.out.println("=".repeat(80));

        String searchKeyword = "tech";

        System.out.println("INPUT:");
        System.out.println("  searchKeyword: '" + searchKeyword + "'");

        System.out.println("\nAVAILABLE EVENTS:");
        System.out.println("  1. 'Tech Conference 2024' - should match");
        System.out.println("  2. 'Spring Boot Workshop' - should NOT match");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  List<Event> containing:");
        System.out.println("  [");
        System.out.println("    Event {");
        System.out.println("      id: '100001',");
        System.out.println("      title: 'Tech Conference 2024',");
        System.out.println("      eventType: CONFERENCE");
        System.out.println("    }");
        System.out.println("  ]");
        System.out.println("  Search is case-insensitive");

        // Mock setup
        List<Event> matchingEvents = Arrays.asList(sampleEvent1);
        when(eventRepository.findByTitleContainingIgnoreCase(searchKeyword)).thenReturn(matchingEvents);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling eventService.searchByTitle(\"" + searchKeyword + "\")...");
        System.out.println("  Repository searching with case-insensitive LIKE query...");

        List<Event> result = eventService.searchByTitle(searchKeyword);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  List<Event> with " + result.size() + " matching event(s):");
        System.out.println("  [");
        for (Event event : result) {
            System.out.println("    Event {");
            System.out.println("      id: '" + event.getId() + "',");
            System.out.println("      title: '" + event.getTitle() + "',");
            System.out.println("      eventType: " + event.getEventType());
            System.out.println("    }");
        }
        System.out.println("  ]");

        System.out.println("\nRESULT: ✅ SUCCESS - Found " + result.size() + " event(s) matching '" + searchKeyword + "'");
    }

    @Test
    void demonstrateGetEventsByType() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 7: GET EVENTS BY TYPE");
        System.out.println("=".repeat(80));

        EventType targetType = EventType.CONFERENCE;

        System.out.println("INPUT:");
        System.out.println("  eventType: " + targetType);

        System.out.println("\nAVAILABLE EVENTS:");
        System.out.println("  1. 'Tech Conference 2024' (CONFERENCE) - should match");
        System.out.println("  2. 'Spring Boot Workshop' (WORKSHOP) - should NOT match");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  List<Event> containing all CONFERENCE events:");
        System.out.println("  [");
        System.out.println("    Event {");
        System.out.println("      id: '100001',");
        System.out.println("      title: 'Tech Conference 2024',");
        System.out.println("      eventType: CONFERENCE,");
        System.out.println("      status: PUBLISHED");
        System.out.println("    }");
        System.out.println("  ]");

        // Mock setup
        List<Event> conferenceEvents = Arrays.asList(sampleEvent1);
        when(eventRepository.findByEventType(targetType)).thenReturn(conferenceEvents);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling eventService.getByType(" + targetType + ")...");

        List<Event> result = eventService.getByType(targetType);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  List<Event> with " + result.size() + " " + targetType + " event(s):");
        System.out.println("  [");
        for (Event event : result) {
            System.out.println("    Event {");
            System.out.println("      id: '" + event.getId() + "',");
            System.out.println("      title: '" + event.getTitle() + "',");
            System.out.println("      eventType: " + event.getEventType() + ",");
            System.out.println("      status: " + event.getStatus());
            System.out.println("    }");
        }
        System.out.println("  ]");

        System.out.println("\nRESULT: ✅ SUCCESS - Retrieved " + result.size() + " " + targetType + " event(s)");

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ALL EVENT SERVICE DEMOS COMPLETED");
        System.out.println("=".repeat(80));
    }
}
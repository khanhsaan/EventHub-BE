package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.MyRegistrationDTO;
import com.csci334.EventHub.dto.RegistrationOutDTO;
import com.csci334.EventHub.dto.RegistrationRequestDTO;
import com.csci334.EventHub.entity.*;
import com.csci334.EventHub.entity.enums.*;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import com.csci334.EventHub.repository.UserRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationService Tests")
class RegistrationServiceTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Event testEvent;
    private User testUser;
    private Registration testRegistration;
    private RegistrationRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup test event
        testEvent = new Event();
        testEvent.setId("100001");
        testEvent.setTitle("Tech Conference 2024");
        testEvent.setDescription("Annual technology conference");
        testEvent.setLocation("Convention Center");
        testEvent.setEventDate(LocalDate.of(2024, 6, 15));
        testEvent.setStartTime(LocalTime.of(9, 0));
        testEvent.setEndTime(LocalTime.of(17, 0));
        testEvent.setEventType(EventType.CONFERENCE);
        testEvent.setStatus(EventStatus.PUBLISHED);
        testEvent.setGeneralPrice(100.0);
        testEvent.setVipPrice(250.0);
        testEvent.setGeneralTicketLimit(500);
        testEvent.setVipTicketLimit(100);
        testEvent.setGeneralTicketsRemaining(450);
        testEvent.setVipTicketsRemaining(95);

        // Setup test user
        testUser = new User();
        testUser.setId("123456");
        testUser.setEmail("attendee@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.ATTENDEE);

        // Setup test registration
        testRegistration = new Registration();
        testRegistration.setId("REG001");
        testRegistration.setEvent(testEvent);
        testRegistration.setAttendee(testUser);
        testRegistration.setStatus(RegistrationStatus.PENDING);
        testRegistration.setRequestedTicketType(TicketType.GENERAL);
        testRegistration.setAmountPaid(0.0);
        testRegistration.setAmountDue(100.0);

        // Setup valid request DTO
        validRequestDTO = new RegistrationRequestDTO();
        validRequestDTO.setEventId("100001");
        validRequestDTO.setAttendeeId("123456");
        validRequestDTO.setTicketType(TicketType.GENERAL);
    }

    @Nested
    @DisplayName("Read Operations Tests")
    class ReadOperationTests {

        @Test
        @DisplayName("Should return all registrations when getAll is called")
        void getAll_ShouldReturnAllRegistrations() {
            // Arrange
            System.out.println("\n=== TEST: Get All Registrations ===");
            List<Registration> allRegistrations = Arrays.asList(testRegistration);
            when(registrationRepository.findAll()).thenReturn(allRegistrations);

            System.out.println("Expected Output: List of " + allRegistrations.size() + " registrations");

            // Act
            List<Registration> result = registrationService.getAll();

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " registrations");
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testRegistration, result.get(0));

            verify(registrationRepository).findAll();
            System.out.println("✅ TEST PASSED: All registrations retrieved successfully!");
        }

        @Test
        @DisplayName("Should return empty list when no registrations exist")
        void getAll_ShouldReturnEmptyList_WhenNoRegistrationsExist() {
            // Arrange
            System.out.println("\n=== TEST: Get All Registrations - Empty Database ===");
            when(registrationRepository.findAll()).thenReturn(Collections.emptyList());

            System.out.println("Expected Output: Empty list");

            // Act
            List<Registration> result = registrationService.getAll();

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " registrations");
            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(registrationRepository).findAll();
            System.out.println("✅ TEST PASSED: Empty list returned correctly!");
        }

        @Test
        @DisplayName("Should return registrations by event ID as DTOs")
        void getByEventId_ShouldReturnRegistrationDTOs() {
            // Arrange
            System.out.println("\n=== TEST: Get Registrations By Event ID ===");
            String eventId = testEvent.getId();
            List<Registration> eventRegistrations = Arrays.asList(testRegistration);
            System.out.println("Input Event ID: " + eventId);

            when(registrationRepository.findByEventId(eventId)).thenReturn(eventRegistrations);

            System.out.println("Expected Output: List of " + eventRegistrations.size() + " RegistrationOutDTOs");

            // Act
            List<RegistrationOutDTO> result = registrationService.getByEventId(eventId);

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " RegistrationOutDTOs");
            assertNotNull(result);
            assertEquals(1, result.size());

            RegistrationOutDTO dto = result.get(0);
            assertEquals(testRegistration.getId(), dto.getRegistrationId());
            assertEquals(testUser.getId(), dto.getAttendeeId());
            assertEquals(testUser.getFullName(), dto.getFullName());
            assertEquals(testUser.getEmail(), dto.getEmail());
            assertEquals(TicketType.GENERAL, dto.getTicketRequested());
            assertEquals(RegistrationStatus.PENDING, dto.getRegistrationStatus());
            assertEquals(100.0, dto.getAmountDue());

            verify(registrationRepository).findByEventId(eventId);
            System.out.println("✅ TEST PASSED: Event registrations converted to DTOs successfully!");
        }

        @Test
        @DisplayName("Should return user registrations as MyRegistrationDTOs")
        void getByUserId_ShouldReturnMyRegistrationDTOs() {
            // Arrange
            System.out.println("\n=== TEST: Get Registrations By User ID ===");
            String userId = testUser.getId();
            List<Registration> userRegistrations = Arrays.asList(testRegistration);
            System.out.println("Input User ID: " + userId);

            when(registrationRepository.findByAttendeeId(userId)).thenReturn(userRegistrations);

            System.out.println("Expected Output: List of " + userRegistrations.size() + " MyRegistrationDTOs");

            // Act
            List<MyRegistrationDTO> result = registrationService.getByUserId(userId);

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " MyRegistrationDTOs");
            assertNotNull(result);
            assertEquals(1, result.size());

            MyRegistrationDTO dto = result.get(0);
            assertEquals(testRegistration.getId(), dto.getRegistrationId());
            assertEquals(testUser.getId(), dto.getAttendeeId());
            assertEquals(testEvent.getTitle(), dto.getEventTitle());
            assertEquals(testEvent.getLocation(), dto.getLocation());
            assertEquals(testEvent.getEventDate(), dto.getDate());
            assertEquals(TicketType.GENERAL, dto.getTicketType());
            assertEquals(RegistrationStatus.PENDING, dto.getStatus());
            assertEquals(100.0, dto.getAmountDue());

            verify(registrationRepository).findByAttendeeId(userId);
            System.out.println("✅ TEST PASSED: User registrations converted to DTOs successfully!");
        }
    }

    @Nested
    @DisplayName("Create Registration Tests")
    class CreateRegistrationTests {

        @Test
        @DisplayName("Should create registration successfully when valid data provided")
        void create_ShouldCreateRegistration_WhenValidDataProvided() {
            // Arrange
            System.out.println("\n=== TEST: Create Registration - Success ===");
            System.out.println("Input RegistrationRequestDTO:");
            System.out.println("  Event ID: " + validRequestDTO.getEventId());
            System.out.println("  Attendee ID: " + validRequestDTO.getAttendeeId());
            System.out.println("  Ticket Type: " + validRequestDTO.getTicketType());

            when(eventRepository.findById(validRequestDTO.getEventId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(validRequestDTO.getAttendeeId())).thenReturn(Optional.of(testUser));
            when(registrationRepository.existsByEventIdAndAttendeeId(validRequestDTO.getEventId(), validRequestDTO.getAttendeeId())).thenReturn(false);
            when(registrationRepository.save(any(Registration.class))).thenReturn(testRegistration);

            System.out.println("Expected Output: Registration created with PENDING status");

            // Act
            Registration result = registrationService.create(validRequestDTO);

            // Assert
            System.out.println("Actual Output: Registration created with ID " + result.getId());
            assertNotNull(result);
            assertEquals(testRegistration, result);

            // Verify interactions
            verify(eventRepository).findById(validRequestDTO.getEventId());
            verify(userRepository).findById(validRequestDTO.getAttendeeId());
            verify(registrationRepository).existsByEventIdAndAttendeeId(validRequestDTO.getEventId(), validRequestDTO.getAttendeeId());

            ArgumentCaptor<Registration> registrationCaptor = ArgumentCaptor.forClass(Registration.class);
            verify(registrationRepository).save(registrationCaptor.capture());

            Registration savedRegistration = registrationCaptor.getValue();
            assertEquals(testEvent, savedRegistration.getEvent());
            assertEquals(testUser, savedRegistration.getAttendee());
            assertEquals(RegistrationStatus.PENDING, savedRegistration.getStatus());
            assertEquals(TicketType.GENERAL, savedRegistration.getRequestedTicketType());
            assertEquals(0.0, savedRegistration.getAmountPaid());
            assertEquals(100.0, savedRegistration.getAmountDue()); // General price

            System.out.println("✅ TEST PASSED: Registration created successfully!");
        }

        @Test
        @DisplayName("Should calculate VIP price correctly when VIP ticket requested")
        void create_ShouldCalculateVIPPrice_WhenVIPTicketRequested() {
            // Arrange
            System.out.println("\n=== TEST: Create Registration - VIP Pricing ===");
            validRequestDTO.setTicketType(TicketType.VIP);
            System.out.println("Input Ticket Type: " + validRequestDTO.getTicketType());
            System.out.println("Expected VIP Price: " + testEvent.getVipPrice());

            when(eventRepository.findById(validRequestDTO.getEventId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(validRequestDTO.getAttendeeId())).thenReturn(Optional.of(testUser));
            when(registrationRepository.existsByEventIdAndAttendeeId(validRequestDTO.getEventId(), validRequestDTO.getAttendeeId())).thenReturn(false);
            when(registrationRepository.save(any(Registration.class))).thenReturn(testRegistration);

            // Act
            registrationService.create(validRequestDTO);

            // Assert
            ArgumentCaptor<Registration> registrationCaptor = ArgumentCaptor.forClass(Registration.class);
            verify(registrationRepository).save(registrationCaptor.capture());

            Registration savedRegistration = registrationCaptor.getValue();
            System.out.println("Actual Amount Due: " + savedRegistration.getAmountDue());
            assertEquals(250.0, savedRegistration.getAmountDue()); // VIP price
            assertEquals(TicketType.VIP, savedRegistration.getRequestedTicketType());

            System.out.println("✅ TEST PASSED: VIP pricing calculated correctly!");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when event does not exist")
        void create_ShouldThrowException_WhenEventNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Create Registration - Event Not Found ===");
            String nonExistentEventId = "999999";
            validRequestDTO.setEventId(nonExistentEventId);
            System.out.println("Input Event ID: " + nonExistentEventId + " (does not exist)");

            when(eventRepository.findById(nonExistentEventId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: EntityNotFoundException");

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> registrationService.create(validRequestDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Event not found", exception.getMessage());

            verify(eventRepository).findById(nonExistentEventId);
            verify(userRepository, never()).findById(any());
            verify(registrationRepository, never()).save(any(Registration.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing event!");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user does not exist")
        void create_ShouldThrowException_WhenUserNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Create Registration - User Not Found ===");
            String nonExistentUserId = "999999";
            validRequestDTO.setAttendeeId(nonExistentUserId);
            System.out.println("Input Attendee ID: " + nonExistentUserId + " (does not exist)");

            when(eventRepository.findById(validRequestDTO.getEventId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: EntityNotFoundException");

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> registrationService.create(validRequestDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("User not found", exception.getMessage());

            verify(eventRepository).findById(validRequestDTO.getEventId());
            verify(userRepository).findById(nonExistentUserId);
            verify(registrationRepository, never()).save(any(Registration.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing user!");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user already registered")
        void create_ShouldThrowException_WhenUserAlreadyRegistered() {
            // Arrange
            System.out.println("\n=== TEST: Create Registration - Duplicate Registration ===");
            System.out.println("User " + validRequestDTO.getAttendeeId() + " already registered for event " + validRequestDTO.getEventId());

            when(eventRepository.findById(validRequestDTO.getEventId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(validRequestDTO.getAttendeeId())).thenReturn(Optional.of(testUser));
            when(registrationRepository.existsByEventIdAndAttendeeId(validRequestDTO.getEventId(), validRequestDTO.getAttendeeId())).thenReturn(true);

            System.out.println("Expected Output: IllegalArgumentException");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> registrationService.create(validRequestDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("You have already registered for this event.", exception.getMessage());

            verify(registrationRepository).existsByEventIdAndAttendeeId(validRequestDTO.getEventId(), validRequestDTO.getAttendeeId());
            verify(registrationRepository, never()).save(any(Registration.class));
            System.out.println("✅ TEST PASSED: Duplicate registration prevented!");
        }
    }

    @Nested
    @DisplayName("Approve Registration Tests")
    class ApproveRegistrationTests {

        @Test
        @DisplayName("Should approve registration with payment required")
        void approveRegistration_ShouldApproveRegistration_WhenPaymentRequired() {
            // Arrange
            System.out.println("\n=== TEST: Approve Registration - Payment Required ===");
            String registrationId = testRegistration.getId();
            System.out.println("Input Registration ID: " + registrationId);
            System.out.println("Current Status: " + testRegistration.getStatus());
            System.out.println("Amount Due: " + testRegistration.getAmountDue());

            when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(testRegistration));
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);

            System.out.println("Expected Output: Status changed to APPROVED");

            // Act
            Registration result = registrationService.approveRegistration(registrationId);

            // Assert
            System.out.println("Actual Output: Status = " + result.getStatus());
            assertEquals(RegistrationStatus.APPROVED, testRegistration.getStatus());
            assertNull(testRegistration.getTicket()); // No ticket issued yet

            verify(registrationRepository).findById(registrationId);
            verify(registrationRepository).save(testRegistration);
            verify(eventRepository, never()).save(any(Event.class)); // No capacity change yet
            System.out.println("✅ TEST PASSED: Registration approved, payment required!");
        }

        @Test
        @DisplayName("Should approve registration and issue ticket when no payment required")
        void approveRegistration_ShouldIssueTicket_WhenNoPaymentRequired() {
            // Arrange
            System.out.println("\n=== TEST: Approve Registration - Free Event ===");
            testRegistration.setAmountDue(0.0); // Free event
            String registrationId = testRegistration.getId();
            System.out.println("Input Registration ID: " + registrationId);
            System.out.println("Amount Due: " + testRegistration.getAmountDue() + " (free event)");

            when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(testRegistration));
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(eventRepository.save(testEvent)).thenReturn(testEvent);

            System.out.println("Expected Output: Status = PAID, Ticket issued, Capacity reduced");

            // Act
            Registration result = registrationService.approveRegistration(registrationId);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  Status: " + result.getStatus());
            System.out.println("  Ticket: " + (result.getTicket() != null ? "ISSUED" : "NOT ISSUED"));
            System.out.println("  Remaining General Tickets: " + testEvent.getGeneralTicketsRemaining());

            assertEquals(RegistrationStatus.PAID, testRegistration.getStatus());
            assertNotNull(testRegistration.getTicket());
            assertEquals(TicketType.GENERAL, testRegistration.getTicket().getTicketType());
            assertEquals(TicketStatus.ISSUED, testRegistration.getTicket().getStatus());
            assertEquals(449, testEvent.getGeneralTicketsRemaining()); // Decreased by 1

            verify(registrationRepository).save(testRegistration);
            verify(eventRepository).save(testEvent);
            System.out.println("✅ TEST PASSED: Free registration completed and ticket issued!");
        }

        @Test
        @DisplayName("Should handle VIP ticket capacity when approving free VIP registration")
        void approveRegistration_ShouldHandleVIPCapacity_WhenApprovingVIPRegistration() {
            // Arrange
            System.out.println("\n=== TEST: Approve Registration - Free VIP Event ===");
            testRegistration.setAmountDue(0.0);
            testRegistration.setRequestedTicketType(TicketType.VIP);
            String registrationId = testRegistration.getId();
            System.out.println("Ticket Type: " + testRegistration.getRequestedTicketType());
            System.out.println("Initial VIP Tickets Remaining: " + testEvent.getVipTicketsRemaining());

            when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(testRegistration));
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(eventRepository.save(testEvent)).thenReturn(testEvent);

            System.out.println("Expected Output: VIP capacity reduced");

            // Act
            registrationService.approveRegistration(registrationId);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  VIP Tickets Remaining: " + testEvent.getVipTicketsRemaining());
            System.out.println("  General Tickets Remaining: " + testEvent.getGeneralTicketsRemaining() + " (unchanged)");

            assertEquals(94, testEvent.getVipTicketsRemaining()); // Decreased by 1
            assertEquals(450, testEvent.getGeneralTicketsRemaining()); // Unchanged
            assertEquals(TicketType.VIP, testRegistration.getTicket().getTicketType());

            verify(eventRepository).save(testEvent);
            System.out.println("✅ TEST PASSED: VIP capacity managed correctly!");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when registration does not exist")
        void approveRegistration_ShouldThrowException_WhenRegistrationNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Approve Registration - Registration Not Found ===");
            String nonExistentId = "999999";
            System.out.println("Input Registration ID: " + nonExistentId + " (does not exist)");

            when(registrationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: EntityNotFoundException");

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> registrationService.approveRegistration(nonExistentId)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Registration not found", exception.getMessage());

            verify(registrationRepository).findById(nonExistentId);
            verify(registrationRepository, never()).save(any(Registration.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing registration!");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when registration is not PENDING")
        void approveRegistration_ShouldThrowException_WhenNotPendingStatus() {
            // Arrange
            System.out.println("\n=== TEST: Approve Registration - Invalid Status ===");
            testRegistration.setStatus(RegistrationStatus.APPROVED);
            String registrationId = testRegistration.getId();
            System.out.println("Current Status: " + testRegistration.getStatus() + " (not PENDING)");

            when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(testRegistration));

            System.out.println("Expected Output: IllegalStateException");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> registrationService.approveRegistration(registrationId)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Only PENDING registrations can be approved.", exception.getMessage());

            verify(registrationRepository).findById(registrationId);
            verify(registrationRepository, never()).save(any(Registration.class));
            System.out.println("✅ TEST PASSED: Invalid status transition prevented!");
        }
    }

    @Nested
    @DisplayName("Reject Registration Tests")
    class RejectRegistrationTests {

        @Test
        @DisplayName("Should reject registration successfully when status is PENDING")
        void rejectRegistration_ShouldRejectRegistration_WhenStatusIsPending() {
            // Arrange
            System.out.println("\n=== TEST: Reject Registration - Success ===");
            String registrationId = testRegistration.getId();
            System.out.println("Input Registration ID: " + registrationId);
            System.out.println("Current Status: " + testRegistration.getStatus());

            when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(testRegistration));
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);

            System.out.println("Expected Output: Status changed to REJECTED");

            // Act
            Registration result = registrationService.rejectRegistration(registrationId);

            // Assert
            System.out.println("Actual Output: Status = " + result.getStatus());
            assertEquals(RegistrationStatus.REJECTED, testRegistration.getStatus());

            verify(registrationRepository).findById(registrationId);
            verify(registrationRepository).save(testRegistration);
            System.out.println("✅ TEST PASSED: Registration rejected successfully!");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when registration does not exist")
        void rejectRegistration_ShouldThrowException_WhenRegistrationNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Reject Registration - Registration Not Found ===");
            String nonExistentId = "999999";
            System.out.println("Input Registration ID: " + nonExistentId + " (does not exist)");

            when(registrationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: EntityNotFoundException");

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> registrationService.rejectRegistration(nonExistentId)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Registration not found", exception.getMessage());

            verify(registrationRepository).findById(nonExistentId);
            verify(registrationRepository, never()).save(any(Registration.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing registration!");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when registration is not PENDING")
        void rejectRegistration_ShouldThrowException_WhenNotPendingStatus() {
            // Arrange
            System.out.println("\n=== TEST: Reject Registration - Invalid Status ===");
            testRegistration.setStatus(RegistrationStatus.APPROVED);
            String registrationId = testRegistration.getId();
            System.out.println("Current Status: " + testRegistration.getStatus() + " (not PENDING)");

            when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(testRegistration));

            System.out.println("Expected Output: IllegalStateException");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> registrationService.rejectRegistration(registrationId)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Only PENDING registrations can be rejected.", exception.getMessage());

            verify(registrationRepository).findById(registrationId);
            verify(registrationRepository, never()).save(any(Registration.class));
            System.out.println("✅ TEST PASSED: Invalid status transition prevented!");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle registration with payment and ticket information in DTO")
        void getByUserId_ShouldIncludePaymentAndTicketInfo_WhenAvailable() {
            // Arrange
            System.out.println("\n=== TEST: Get User Registrations - With Payment & Ticket ===");

            // Add ticket to registration
            Ticket ticket = new Ticket();
            ticket.setTicketCode("TECH-12345678");
            ticket.setTicketType(TicketType.GENERAL);
            ticket.setStatus(TicketStatus.ISSUED);
            testRegistration.setTicket(ticket);

            // Add payment to registration
            Payment payment = new Payment();
            payment.setCardLastFour("1234");
            testRegistration.setPayment(payment);

            String userId = testUser.getId();
            when(registrationRepository.findByAttendeeId(userId)).thenReturn(Arrays.asList(testRegistration));

            System.out.println("Expected Output: DTO with ticket code and card info");

            // Act
            List<MyRegistrationDTO> result = registrationService.getByUserId(userId);

            // Assert
            MyRegistrationDTO dto = result.get(0);
            System.out.println("Actual Output:");
            System.out.println("  Ticket Code: " + dto.getTicketCode());
            System.out.println("  Card Last Four: " + dto.getCardLastFour());

            assertEquals("TECH-12345678", dto.getTicketCode());
            assertEquals("1234", dto.getCardLastFour());

            System.out.println("✅ TEST PASSED: Payment and ticket info included in DTO!");
        }

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void methods_ShouldHandleNullInputs() {
            System.out.println("\n=== TEST: Handle Null Inputs ===");

            // Test getByEventId with null
            when(registrationRepository.findByEventId(null)).thenReturn(Collections.emptyList());
            List<RegistrationOutDTO> result1 = registrationService.getByEventId(null);
            assertNotNull(result1);
            assertTrue(result1.isEmpty());

            // Test getByUserId with null
            when(registrationRepository.findByAttendeeId(null)).thenReturn(Collections.emptyList());
            List<MyRegistrationDTO> result2 = registrationService.getByUserId(null);
            assertNotNull(result2);
            assertTrue(result2.isEmpty());

            System.out.println("✅ TEST PASSED: Null inputs handled gracefully!");
        }

        @Test
        @DisplayName("Should handle repository save failures")
        void create_ShouldPropagateException_WhenRepositoryFails() {
            // Arrange
            System.out.println("\n=== TEST: Repository Save Failure ===");
            when(eventRepository.findById(validRequestDTO.getEventId())).thenReturn(Optional.of(testEvent));
            when(userRepository.findById(validRequestDTO.getAttendeeId())).thenReturn(Optional.of(testUser));
            when(registrationRepository.existsByEventIdAndAttendeeId(any(), any())).thenReturn(false);
            when(registrationRepository.save(any(Registration.class))).thenThrow(new RuntimeException("Database error"));

            System.out.println("Expected Output: RuntimeException propagated");

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> registrationService.create(validRequestDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Database error", exception.getMessage());
            System.out.println("✅ TEST PASSED: Repository exception propagated correctly!");
        }
    }
}
package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.PaymentMakeDTO;
import com.csci334.EventHub.dto.analytics.AnalyticsOverviewDTO;
import com.csci334.EventHub.dto.analytics.SalesEntryDTO;
import com.csci334.EventHub.entity.*;
import com.csci334.EventHub.entity.enums.*;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.PaymentRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private NotificationService notificationService;
    @Mock private AnalyticsService analyticsService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private Event testEvent;
    private User testUser;
    private User testOrganizer;
    private Registration testRegistration;
    private Payment testPayment;
    private PaymentMakeDTO validPaymentDTO;

    @BeforeEach
    void setUp() {
        // Setup test organizer
        testOrganizer = new User();
        testOrganizer.setId("ORG001");
        testOrganizer.setEmail("organizer@example.com");
        testOrganizer.setFirstName("Event");
        testOrganizer.setLastName("Organizer");
        testOrganizer.setRole(Role.ORGANIZER);

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
        testEvent.setOrganizer(testOrganizer);

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
        testRegistration.setStatus(RegistrationStatus.APPROVED);
        testRegistration.setRequestedTicketType(TicketType.GENERAL);
        testRegistration.setAmountPaid(0.0);
        testRegistration.setAmountDue(100.0);

        // Setup test payment
        testPayment = new Payment();
        testPayment.setId("PAY001");
        testPayment.setRegistration(testRegistration);
        testPayment.setAmount(100.0);
        testPayment.setTransactionId("TXN-12345");
        testPayment.setCardLastFour("1234");
        testPayment.setStatus("SUCCESS");
        testPayment.setPaidAt(LocalDateTime.now());
        testPayment.setRefundStatus(RefundStatus.NONE);

        // Setup valid payment DTO
        validPaymentDTO = new PaymentMakeDTO();
        validPaymentDTO.setRegistrationId("REG001");
        validPaymentDTO.setCardLastFour("1234");
    }

    @Nested
    @DisplayName("Make Payment Tests")
    class MakePaymentTests {

        @Test
        @DisplayName("Should process payment successfully for approved registration")
        void makePayment_ShouldProcessPayment_WhenRegistrationIsApproved() {
            // Arrange
            System.out.println("\n=== TEST: Make Payment - Success ===");
            System.out.println("Input PaymentMakeDTO:");
            System.out.println("  Registration ID: " + validPaymentDTO.getRegistrationId());
            System.out.println("  Card Last Four: " + validPaymentDTO.getCardLastFour());
            System.out.println("Registration Status: " + testRegistration.getStatus());
            System.out.println("Amount Due: $" + testRegistration.getAmountDue());

            when(registrationRepository.findById(validPaymentDTO.getRegistrationId())).thenReturn(Optional.of(testRegistration));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(eventRepository.save(testEvent)).thenReturn(testEvent);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(analyticsService.getOverview(testOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected Output: Payment processed, ticket issued, capacity reduced");

            // Act
            Payment result = paymentService.makePayment(validPaymentDTO);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  Payment processed with transaction ID: " + result.getTransactionId());
            System.out.println("  Registration status: " + testRegistration.getStatus());
            System.out.println("  Ticket status: " + (testRegistration.getTicket() != null ? testRegistration.getTicket().getStatus() : "null"));
            System.out.println("  General tickets remaining: " + testEvent.getGeneralTicketsRemaining());

            assertNotNull(result);
            assertEquals("SUCCESS", result.getStatus());
            assertEquals(RegistrationStatus.PAID, testRegistration.getStatus());
            assertNotNull(testRegistration.getTicket());
            assertEquals(TicketStatus.ISSUED, testRegistration.getTicket().getStatus());
            assertEquals(TicketType.GENERAL, testRegistration.getTicket().getTicketType());
            assertEquals(100.0, testRegistration.getAmountPaid());
            assertEquals(449, testEvent.getGeneralTicketsRemaining()); // Decreased by 1

            // Verify interactions
            verify(registrationRepository).findById(validPaymentDTO.getRegistrationId());
            verify(paymentRepository).save(any(Payment.class));
            verify(eventRepository).save(testEvent);
            verify(registrationRepository).save(testRegistration);
            verify(messagingTemplate).convertAndSend(eq("/topic/sales/" + testOrganizer.getId()), any(SalesEntryDTO.class));
            verify(messagingTemplate).convertAndSend(eq("/topic/analytics/" + testOrganizer.getId()), any(AnalyticsOverviewDTO.class));

            System.out.println("✅ TEST PASSED: Payment processed successfully!");
        }

        @Test
        @DisplayName("Should handle VIP ticket payment with correct capacity management")
        void makePayment_ShouldHandleVIPTicket_WithCorrectCapacityManagement() {
            // Arrange
            System.out.println("\n=== TEST: Make Payment - VIP Ticket ===");
            testRegistration.setRequestedTicketType(TicketType.VIP);
            testRegistration.setAmountDue(250.0);
            System.out.println("Ticket Type: " + testRegistration.getRequestedTicketType());
            System.out.println("Amount Due: $" + testRegistration.getAmountDue());
            System.out.println("Initial VIP Tickets Remaining: " + testEvent.getVipTicketsRemaining());

            when(registrationRepository.findById(validPaymentDTO.getRegistrationId())).thenReturn(Optional.of(testRegistration));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(eventRepository.save(testEvent)).thenReturn(testEvent);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(analyticsService.getOverview(testOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected Output: VIP capacity reduced, general capacity unchanged");

            // Act
            paymentService.makePayment(validPaymentDTO);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  VIP Tickets Remaining: " + testEvent.getVipTicketsRemaining());
            System.out.println("  General Tickets Remaining: " + testEvent.getGeneralTicketsRemaining() + " (unchanged)");
            System.out.println("  Ticket Type: " + testRegistration.getTicket().getTicketType());

            assertEquals(94, testEvent.getVipTicketsRemaining()); // Decreased by 1
            assertEquals(450, testEvent.getGeneralTicketsRemaining()); // Unchanged
            assertEquals(TicketType.VIP, testRegistration.getTicket().getTicketType());

            // Verify VIP sales tracking
            ArgumentCaptor<SalesEntryDTO> salesCaptor = ArgumentCaptor.forClass(SalesEntryDTO.class);
            verify(messagingTemplate).convertAndSend(eq("/topic/sales/" + testOrganizer.getId()), salesCaptor.capture());
            SalesEntryDTO salesDto = salesCaptor.getValue();
            assertEquals(1, salesDto.getVip());
            assertEquals(0, salesDto.getGeneral());

            System.out.println("✅ TEST PASSED: VIP payment processed with correct capacity management!");
        }

        @Test
        @DisplayName("Should throw RuntimeException when registration not found")
        void makePayment_ShouldThrowException_WhenRegistrationNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Make Payment - Registration Not Found ===");
            String nonExistentId = "999999";
            validPaymentDTO.setRegistrationId(nonExistentId);
            System.out.println("Input Registration ID: " + nonExistentId + " (does not exist)");

            when(registrationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: RuntimeException");

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> paymentService.makePayment(validPaymentDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Registration not found", exception.getMessage());

            verify(registrationRepository).findById(nonExistentId);
            verify(paymentRepository, never()).save(any(Payment.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing registration!");
        }

        @Test
        @DisplayName("Should throw RuntimeException when registration is not approved")
        void makePayment_ShouldThrowException_WhenRegistrationNotApproved() {
            // Arrange
            System.out.println("\n=== TEST: Make Payment - Invalid Registration Status ===");
            testRegistration.setStatus(RegistrationStatus.PENDING);
            System.out.println("Registration Status: " + testRegistration.getStatus() + " (not APPROVED)");

            when(registrationRepository.findById(validPaymentDTO.getRegistrationId())).thenReturn(Optional.of(testRegistration));

            System.out.println("Expected Output: RuntimeException");

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> paymentService.makePayment(validPaymentDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Only approved registrations can be paid for.", exception.getMessage());

            verify(registrationRepository).findById(validPaymentDTO.getRegistrationId());
            verify(paymentRepository, never()).save(any(Payment.class));
            System.out.println("✅ TEST PASSED: Invalid registration status prevented payment!");
        }
    }

    @Nested
    @DisplayName("Refund by Organizer Tests")
    class RefundByOrganizerTests {

        @Test
        @DisplayName("Should process organizer refund successfully")
        void refundByOrganizer_ShouldProcessRefund_Successfully() {
            // Arrange
            System.out.println("\n=== TEST: Refund by Organizer - Success ===");
            String registrationId = testRegistration.getId();
            String refundReason = "Event cancelled by organizer";
            System.out.println("Input Registration ID: " + registrationId);
            System.out.println("Refund Reason: " + refundReason);

            // Setup ticket for the registration
            Ticket ticket = new Ticket();
            ticket.setTicketType(TicketType.GENERAL);
            ticket.setStatus(TicketStatus.ISSUED);
            testRegistration.setTicket(ticket);
            testRegistration.setStatus(RegistrationStatus.PAID);

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(testPayment)).thenReturn(testPayment);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(analyticsService.getOverview(testOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
            doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString());
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected Output: Refund processed, capacity restored, notifications sent");

            // Act
            Payment result = paymentService.refundByOrganizer(registrationId, refundReason);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  Payment status: " + result.getStatus());
            System.out.println("  Refund status: " + result.getRefundStatus());
            System.out.println("  Registration status: " + testRegistration.getStatus());
            System.out.println("  Ticket status: " + ticket.getStatus());
            System.out.println("  General tickets remaining: " + testEvent.getGeneralTicketsRemaining());

            assertEquals("REFUNDED", result.getStatus());
            assertEquals(RefundStatus.COMPLETED, result.getRefundStatus());
            assertEquals(refundReason, result.getRefundReason());
            assertNotNull(result.getRefundedAt());
            assertEquals(RegistrationStatus.REFUNDED, testRegistration.getStatus());
            assertEquals(TicketStatus.REFUNDED, ticket.getStatus());
            assertEquals(451, testEvent.getGeneralTicketsRemaining()); // Increased by 1

            verify(paymentRepository).findByRegistrationId(registrationId);
            verify(paymentRepository).save(testPayment);
            verify(registrationRepository).save(testRegistration);
            verify(notificationService).sendNotification(eq(testUser.getId()), anyString(), anyString());
            verify(messagingTemplate).convertAndSend(eq("/topic/analytics/" + testOrganizer.getId()), any(AnalyticsOverviewDTO.class));

            System.out.println("✅ TEST PASSED: Organizer refund processed successfully!");
        }

        @Test
        @DisplayName("Should handle VIP ticket refund with correct capacity restoration")
        void refundByOrganizer_ShouldRestoreVIPCapacity_WhenRefundingVIPTicket() {
            // Arrange
            System.out.println("\n=== TEST: Refund by Organizer - VIP Ticket ===");
            String registrationId = testRegistration.getId();
            String refundReason = "Customer request";

            // Setup VIP ticket
            Ticket vipTicket = new Ticket();
            vipTicket.setTicketType(TicketType.VIP);
            vipTicket.setStatus(TicketStatus.ISSUED);
            testRegistration.setTicket(vipTicket);
            testRegistration.setStatus(RegistrationStatus.PAID);

            System.out.println("Ticket Type: " + vipTicket.getTicketType());
            System.out.println("Initial VIP Tickets Remaining: " + testEvent.getVipTicketsRemaining());

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(testPayment)).thenReturn(testPayment);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(analyticsService.getOverview(testOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
            doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString());
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected Output: VIP capacity restored, general capacity unchanged");

            // Act
            paymentService.refundByOrganizer(registrationId, refundReason);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  VIP Tickets Remaining: " + testEvent.getVipTicketsRemaining());
            System.out.println("  General Tickets Remaining: " + testEvent.getGeneralTicketsRemaining() + " (unchanged)");

            assertEquals(96, testEvent.getVipTicketsRemaining()); // Increased by 1
            assertEquals(450, testEvent.getGeneralTicketsRemaining()); // Unchanged

            System.out.println("✅ TEST PASSED: VIP refund processed with correct capacity restoration!");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when payment not found")
        void refundByOrganizer_ShouldThrowException_WhenPaymentNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Refund by Organizer - Payment Not Found ===");
            String nonExistentId = "999999";
            System.out.println("Input Registration ID: " + nonExistentId + " (payment does not exist)");

            when(paymentRepository.findByRegistrationId(nonExistentId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: IllegalArgumentException");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentService.refundByOrganizer(nonExistentId, "reason")
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Payment not found", exception.getMessage());

            verify(paymentRepository).findByRegistrationId(nonExistentId);
            verify(paymentRepository, never()).save(any(Payment.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing payment!");
        }
    }

    @Nested
    @DisplayName("Request Refund Tests")
    class RequestRefundTests {

        @Test
        @DisplayName("Should request refund successfully for paid registration")
        void requestRefund_ShouldRequestRefund_WhenRegistrationIsPaid() {
            // Arrange
            System.out.println("\n=== TEST: Request Refund - Success ===");
            String registrationId = testRegistration.getId();
            String refundReason = "Can't attend anymore";
            testRegistration.setStatus(RegistrationStatus.PAID);
            System.out.println("Input Registration ID: " + registrationId);
            System.out.println("Refund Reason: " + refundReason);
            System.out.println("Registration Status: " + testRegistration.getStatus());

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(testPayment)).thenReturn(testPayment);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);

            System.out.println("Expected Output: Refund requested, status changed to REFUND_REQUESTED");

            // Act
            Payment result = paymentService.requestRefund(registrationId, refundReason);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  Refund status: " + result.getRefundStatus());
            System.out.println("  Refund reason: " + result.getRefundReason());
            System.out.println("  Registration status: " + testRegistration.getStatus());

            assertEquals(RefundStatus.REQUESTED, result.getRefundStatus());
            assertEquals(refundReason, result.getRefundReason());
            assertEquals(RegistrationStatus.REFUND_REQUESTED, testRegistration.getStatus());

            verify(paymentRepository).findByRegistrationId(registrationId);
            verify(paymentRepository).save(testPayment);
            verify(registrationRepository).save(testRegistration);

            System.out.println("✅ TEST PASSED: Refund requested successfully!");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when payment not found")
        void requestRefund_ShouldThrowException_WhenPaymentNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Request Refund - Payment Not Found ===");
            String nonExistentId = "999999";
            System.out.println("Input Registration ID: " + nonExistentId + " (payment does not exist)");

            when(paymentRepository.findByRegistrationId(nonExistentId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: IllegalArgumentException");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentService.requestRefund(nonExistentId, "reason")
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Payment not found", exception.getMessage());

            verify(paymentRepository).findByRegistrationId(nonExistentId);
            verify(paymentRepository, never()).save(any(Payment.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing payment!");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when registration is not paid")
        void requestRefund_ShouldThrowException_WhenRegistrationNotPaid() {
            // Arrange
            System.out.println("\n=== TEST: Request Refund - Invalid Registration Status ===");
            testRegistration.setStatus(RegistrationStatus.APPROVED);
            String registrationId = testRegistration.getId();
            System.out.println("Registration Status: " + testRegistration.getStatus() + " (not PAID)");

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));

            System.out.println("Expected Output: IllegalStateException");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> paymentService.requestRefund(registrationId, "reason")
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Only paid registrations can request a refund.", exception.getMessage());

            verify(paymentRepository).findByRegistrationId(registrationId);
            verify(paymentRepository, never()).save(any(Payment.class));
            System.out.println("✅ TEST PASSED: Invalid registration status prevented refund request!");
        }
    }

    @Nested
    @DisplayName("Approve Refund Tests")
    class ApproveRefundTests {

        @Test
        @DisplayName("Should approve refund successfully when refund is requested")
        void approveRefund_ShouldApproveRefund_WhenRefundIsRequested() {
            // Arrange
            System.out.println("\n=== TEST: Approve Refund - Success ===");
            String registrationId = testRegistration.getId();
            testPayment.setRefundStatus(RefundStatus.REQUESTED);
            testRegistration.setStatus(RegistrationStatus.REFUND_REQUESTED);

            // Setup ticket
            Ticket ticket = new Ticket();
            ticket.setTicketType(TicketType.GENERAL);
            ticket.setStatus(TicketStatus.ISSUED);
            testRegistration.setTicket(ticket);

            System.out.println("Input Registration ID: " + registrationId);
            System.out.println("Current Refund Status: " + testPayment.getRefundStatus());

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(testPayment)).thenReturn(testPayment);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(analyticsService.getOverview(testOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
            doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString());
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected Output: Refund approved and completed");

            // Act
            Payment result = paymentService.approveRefund(registrationId);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  Refund status: " + result.getRefundStatus());
            System.out.println("  Payment status: " + result.getStatus());
            System.out.println("  Registration status: " + testRegistration.getStatus());
            System.out.println("  Ticket status: " + ticket.getStatus());
            System.out.println("  General tickets remaining: " + testEvent.getGeneralTicketsRemaining());

            assertEquals(RefundStatus.COMPLETED, result.getRefundStatus());
            assertEquals("REFUNDED", result.getStatus());
            assertNotNull(result.getRefundedAt());
            assertEquals(RegistrationStatus.REFUNDED, testRegistration.getStatus());
            assertEquals(TicketStatus.REFUNDED, ticket.getStatus());
            assertEquals(451, testEvent.getGeneralTicketsRemaining()); // Increased by 1

            verify(paymentRepository).save(testPayment);
            verify(registrationRepository).save(testRegistration);
            verify(notificationService).sendNotification(eq(testUser.getId()), eq("Refund Approved"), anyString());
            verify(messagingTemplate).convertAndSend(eq("/topic/analytics/" + testOrganizer.getId()), any(AnalyticsOverviewDTO.class));

            System.out.println("✅ TEST PASSED: Refund approved successfully!");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when refund is not in requested state")
        void approveRefund_ShouldThrowException_WhenRefundNotRequested() {
            // Arrange
            System.out.println("\n=== TEST: Approve Refund - Invalid Refund Status ===");
            testPayment.setRefundStatus(RefundStatus.NONE);
            String registrationId = testRegistration.getId();
            System.out.println("Current Refund Status: " + testPayment.getRefundStatus() + " (not REQUESTED)");

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));

            System.out.println("Expected Output: IllegalStateException");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> paymentService.approveRefund(registrationId)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Refund is not in REQUESTED state.", exception.getMessage());

            verify(paymentRepository).findByRegistrationId(registrationId);
            verify(paymentRepository, never()).save(any(Payment.class));
            System.out.println("✅ TEST PASSED: Invalid refund status prevented approval!");
        }
    }

    @Nested
    @DisplayName("Reject Refund Tests")
    class RejectRefundTests {

        @Test
        @DisplayName("Should reject refund successfully when refund is requested")
        void rejectRefund_ShouldRejectRefund_WhenRefundIsRequested() {
            // Arrange
            System.out.println("\n=== TEST: Reject Refund - Success ===");
            String registrationId = testRegistration.getId();
            testPayment.setRefundStatus(RefundStatus.REQUESTED);
            testRegistration.setStatus(RegistrationStatus.REFUND_REQUESTED);

            System.out.println("Input Registration ID: " + registrationId);
            System.out.println("Current Refund Status: " + testPayment.getRefundStatus());

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(testPayment)).thenReturn(testPayment);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString());

            System.out.println("Expected Output: Refund rejected, status reverted to SUCCESS/PAID");

            // Act
            Payment result = paymentService.rejectRefund(registrationId);

            // Assert
            System.out.println("Actual Output:");
            System.out.println("  Refund status: " + result.getRefundStatus());
            System.out.println("  Payment status: " + result.getStatus());
            System.out.println("  Registration status: " + testRegistration.getStatus());

            assertEquals(RefundStatus.REJECTED, result.getRefundStatus());
            assertEquals("SUCCESS", result.getStatus());
            assertEquals(RegistrationStatus.PAID, testRegistration.getStatus());

            verify(paymentRepository).save(testPayment);
            verify(registrationRepository).save(testRegistration);
            verify(notificationService).sendNotification(eq(testUser.getId()), eq("Refund Rejected"), anyString());

            System.out.println("✅ TEST PASSED: Refund rejected successfully!");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when refund is not in requested state")
        void rejectRefund_ShouldThrowException_WhenRefundNotRequested() {
            // Arrange
            System.out.println("\n=== TEST: Reject Refund - Invalid Refund Status ===");
            testPayment.setRefundStatus(RefundStatus.COMPLETED);
            String registrationId = testRegistration.getId();
            System.out.println("Current Refund Status: " + testPayment.getRefundStatus() + " (not REQUESTED)");

            when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(testPayment));

            System.out.println("Expected Output: IllegalStateException");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> paymentService.rejectRefund(registrationId)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Refund is not in REQUESTED state.", exception.getMessage());

            verify(paymentRepository).findByRegistrationId(registrationId);
            verify(paymentRepository, never()).save(any(Payment.class));
            System.out.println("✅ TEST PASSED: Invalid refund status prevented rejection!");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle payment processing with all integrations")
        void makePayment_ShouldHandleAllIntegrations_Successfully() {
            // Arrange
            System.out.println("\n=== TEST: Payment Processing - Full Integration ===");
            when(registrationRepository.findById(validPaymentDTO.getRegistrationId())).thenReturn(Optional.of(testRegistration));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(eventRepository.save(testEvent)).thenReturn(testEvent);
            when(registrationRepository.save(testRegistration)).thenReturn(testRegistration);
            when(analyticsService.getOverview(testOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
            doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

            System.out.println("Expected integrations: Analytics service, WebSocket messaging, capacity management");

            // Act
            Payment result = paymentService.makePayment(validPaymentDTO);

            // Assert
            System.out.println("Actual integrations verified:");
            assertNotNull(result);

            // Verify payment creation with transaction ID generation
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());
            Payment savedPayment = paymentCaptor.getValue();
            assertTrue(savedPayment.getTransactionId().startsWith("TXN-"));
            assertEquals(100.0, savedPayment.getAmount());
            assertEquals("1234", savedPayment.getCardLastFour());

            // Verify sales tracking
            verify(messagingTemplate).convertAndSend(eq("/topic/sales/" + testOrganizer.getId()), any(SalesEntryDTO.class));

            // Verify analytics update
            verify(analyticsService).getOverview(testOrganizer.getId());
            verify(messagingTemplate).convertAndSend(eq("/topic/analytics/" + testOrganizer.getId()), any(AnalyticsOverviewDTO.class));

            System.out.println("  ✓ Transaction ID generation");
            System.out.println("  ✓ Sales tracking WebSocket");
            System.out.println("  ✓ Analytics service integration");
            System.out.println("  ✓ Capacity management");
            System.out.println("✅ TEST PASSED: All integrations working correctly!");
        }

        @Test
        @DisplayName("Should handle repository save failures gracefully")
        void makePayment_ShouldPropagateException_WhenRepositoryFails() {
            // Arrange
            System.out.println("\n=== TEST: Repository Save Failure ===");
            when(registrationRepository.findById(validPaymentDTO.getRegistrationId())).thenReturn(Optional.of(testRegistration));
            when(paymentRepository.save(any(Payment.class))).thenThrow(new RuntimeException("Database error"));

            System.out.println("Expected Output: RuntimeException propagated");

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> paymentService.makePayment(validPaymentDTO)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Database error", exception.getMessage());
            System.out.println("✅ TEST PASSED: Repository exception propagated correctly!");
        }
    }
}
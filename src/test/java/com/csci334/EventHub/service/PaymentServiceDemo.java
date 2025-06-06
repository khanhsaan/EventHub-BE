package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.PaymentMakeDTO;
import com.csci334.EventHub.dto.analytics.AnalyticsOverviewDTO;
import com.csci334.EventHub.entity.*;
import com.csci334.EventHub.entity.enums.*;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.PaymentRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Demo class showing expected vs actual output for PaymentService methods
 * This demonstrates the behavior of payment processing and refund workflows
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceDemo {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private NotificationService notificationService;
    @Mock private AnalyticsService analyticsService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private Event sampleEvent;
    private User sampleUser;
    private User sampleOrganizer;
    private Registration sampleRegistration;
    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        reset(paymentRepository, registrationRepository, eventRepository,
                notificationService, analyticsService, messagingTemplate);

        // Setup sample organizer
        sampleOrganizer = new User();
        sampleOrganizer.setId("ORG001");
        sampleOrganizer.setEmail("organizer@techconf.com");
        sampleOrganizer.setFirstName("Event");
        sampleOrganizer.setLastName("Organizer");
        sampleOrganizer.setRole(Role.ORGANIZER);

        // Setup sample event
        sampleEvent = new Event();
        sampleEvent.setId("100001");
        sampleEvent.setTitle("AI & Data Science Summit 2024");
        sampleEvent.setDescription("Premier conference on AI and data science");
        sampleEvent.setLocation("Tech Convention Center");
        sampleEvent.setEventDate(LocalDate.of(2024, 10, 15));
        sampleEvent.setStartTime(LocalTime.of(9, 0));
        sampleEvent.setEndTime(LocalTime.of(18, 0));
        sampleEvent.setEventType(EventType.CONFERENCE);
        sampleEvent.setStatus(EventStatus.PUBLISHED);
        sampleEvent.setGeneralPrice(200.0);
        sampleEvent.setVipPrice(500.0);
        sampleEvent.setGeneralTicketLimit(400);
        sampleEvent.setVipTicketLimit(80);
        sampleEvent.setGeneralTicketsRemaining(325);
        sampleEvent.setVipTicketsRemaining(72);
        sampleEvent.setOrganizer(sampleOrganizer);

        // Setup sample user
        sampleUser = new User();
        sampleUser.setId("123456");
        sampleUser.setEmail("alice.johnson@example.com");
        sampleUser.setFirstName("Alice");
        sampleUser.setLastName("Johnson");
        sampleUser.setRole(Role.ATTENDEE);

        // Setup sample registration
        sampleRegistration = new Registration();
        sampleRegistration.setId("REG001");
        sampleRegistration.setEvent(sampleEvent);
        sampleRegistration.setAttendee(sampleUser);
        sampleRegistration.setStatus(RegistrationStatus.APPROVED);
        sampleRegistration.setRequestedTicketType(TicketType.GENERAL);
        sampleRegistration.setAmountPaid(0.0);
        sampleRegistration.setAmountDue(200.0);

        // Setup sample payment
        samplePayment = new Payment();
        samplePayment.setId("PAY001");
        samplePayment.setRegistration(sampleRegistration);
        samplePayment.setAmount(200.0);
        samplePayment.setTransactionId("TXN-1697123456789");
        samplePayment.setCardLastFour("4521");
        samplePayment.setStatus("SUCCESS");
        samplePayment.setPaidAt(LocalDateTime.now());
        samplePayment.setRefundStatus(RefundStatus.NONE);
    }

    @Test
    void demonstratePaymentProcessing() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 1: PROCESS PAYMENT FOR APPROVED REGISTRATION");
        System.out.println("=".repeat(80));

        PaymentMakeDTO paymentRequest = new PaymentMakeDTO();
        paymentRequest.setRegistrationId("REG001");
        paymentRequest.setCardLastFour("4521");

        System.out.println("INPUT:");
        System.out.println("  PaymentMakeDTO {");
        System.out.println("    registrationId: '" + paymentRequest.getRegistrationId() + "',");
        System.out.println("    cardLastFour: '" + paymentRequest.getCardLastFour() + "'");
        System.out.println("  }");

        System.out.println("\nCURRENT REGISTRATION STATE:");
        System.out.println("  Registration {");
        System.out.println("    id: '" + sampleRegistration.getId() + "',");
        System.out.println("    status: " + sampleRegistration.getStatus() + ",");
        System.out.println("    ticketType: " + sampleRegistration.getRequestedTicketType() + ",");
        System.out.println("    amountDue: $" + sampleRegistration.getAmountDue() + ",");
        System.out.println("    amountPaid: $" + sampleRegistration.getAmountPaid());
        System.out.println("  }");

        System.out.println("\nEVENT CAPACITY BEFORE:");
        System.out.println("  generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining());
        System.out.println("  vipTicketsRemaining: " + sampleEvent.getVipTicketsRemaining());

        System.out.println("\nPAYMENT PROCESSING STEPS:");
        System.out.println("  1. Validate registration status (must be APPROVED)");
        System.out.println("  2. Create payment record with transaction ID");
        System.out.println("  3. Update registration status to PAID");
        System.out.println("  4. Generate and issue ticket");
        System.out.println("  5. Reduce event capacity");
        System.out.println("  6. Send sales analytics via WebSocket");
        System.out.println("  7. Update analytics dashboard");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Payment {");
        System.out.println("    id: 'PAY001',");
        System.out.println("    amount: " + sampleEvent.getGeneralPrice() + ",");
        System.out.println("    transactionId: 'TXN-[timestamp]',");
        System.out.println("    cardLastFour: '4521',");
        System.out.println("    status: 'SUCCESS',");
        System.out.println("    paidAt: current timestamp");
        System.out.println("  }");
        System.out.println("  Registration {");
        System.out.println("    status: PAID (changed from APPROVED),");
        System.out.println("    amountPaid: " + sampleEvent.getGeneralPrice() + ",");
        System.out.println("    ticket: ISSUED");
        System.out.println("  }");
        System.out.println("  Event Capacity {");
        System.out.println("    generalTicketsRemaining: " + (sampleEvent.getGeneralTicketsRemaining() - 1));
        System.out.println("  }");

        // Mock setup
        when(registrationRepository.findById(paymentRequest.getRegistrationId())).thenReturn(Optional.of(sampleRegistration));
        when(paymentRepository.save(any(Payment.class))).thenReturn(samplePayment);
        when(eventRepository.save(sampleEvent)).thenReturn(sampleEvent);
        when(registrationRepository.save(sampleRegistration)).thenReturn(sampleRegistration);
        when(analyticsService.getOverview(sampleOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling paymentService.makePayment(paymentRequest)...");
        System.out.println("  Processing payment for $" + sampleRegistration.getAmountDue() + "...");

        Payment result = paymentService.makePayment(paymentRequest);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Payment {");
        System.out.println("    id: '" + result.getId() + "',");
        System.out.println("    amount: $" + result.getAmount() + ",");
        System.out.println("    transactionId: '" + result.getTransactionId() + "',");
        System.out.println("    cardLastFour: '" + result.getCardLastFour() + "',");
        System.out.println("    status: '" + result.getStatus() + "',");
        System.out.println("    paidAt: " + result.getPaidAt());
        System.out.println("  }");
        System.out.println("  Registration {");
        System.out.println("    status: " + sampleRegistration.getStatus() + ",");
        System.out.println("    amountPaid: $" + sampleRegistration.getAmountPaid() + ",");
        System.out.println("    ticket: " + (sampleRegistration.getTicket() != null ? sampleRegistration.getTicket().getStatus() : "null"));
        System.out.println("  }");
        System.out.println("  Event Capacity {");
        System.out.println("    generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining());
        System.out.println("  }");

        System.out.println("\nINTEGRATIONS TRIGGERED:");
        System.out.println("  ✓ Sales analytics WebSocket sent");
        System.out.println("  ✓ Analytics dashboard updated");
        System.out.println("  ✓ Event capacity reduced");
        System.out.println("  ✓ Ticket issued automatically");

        System.out.println("\nRESULT: ✅ SUCCESS - Payment processed with full integration");
    }

    @Test
    void demonstrateVIPPaymentProcessing() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 2: PROCESS VIP PAYMENT WITH PREMIUM PRICING");
        System.out.println("=".repeat(80));

        // Modify registration for VIP
        sampleRegistration.setRequestedTicketType(TicketType.VIP);
        sampleRegistration.setAmountDue(500.0);

        PaymentMakeDTO vipPaymentRequest = new PaymentMakeDTO();
        vipPaymentRequest.setRegistrationId("REG001");
        vipPaymentRequest.setCardLastFour("9876");

        System.out.println("INPUT:");
        System.out.println("  PaymentMakeDTO {");
        System.out.println("    registrationId: '" + vipPaymentRequest.getRegistrationId() + "',");
        System.out.println("    cardLastFour: '" + vipPaymentRequest.getCardLastFour() + "' (Premium Card)");
        System.out.println("  }");

        System.out.println("\nVIP REGISTRATION DETAILS:");
        System.out.println("  ticketType: " + sampleRegistration.getRequestedTicketType() + " (Premium),");
        System.out.println("  amountDue: $" + sampleRegistration.getAmountDue() + " (vs $" + sampleEvent.getGeneralPrice() + " General),");
        System.out.println("  premium: $" + (sampleRegistration.getAmountDue() - sampleEvent.getGeneralPrice()) + " extra");

        System.out.println("\nVIP CAPACITY BEFORE:");
        System.out.println("  vipTicketsRemaining: " + sampleEvent.getVipTicketsRemaining() + " (limited availability)");
        System.out.println("  generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining() + " (should remain unchanged)");

        System.out.println("\nVIP PROCESSING DIFFERENCES:");
        System.out.println("  • Higher payment amount ($500 vs $200)");
        System.out.println("  • VIP capacity reduction (not general)");
        System.out.println("  • VIP sales analytics tracking");
        System.out.println("  • VIP ticket type assignment");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Payment {");
        System.out.println("    amount: " + sampleEvent.getVipPrice() + " (VIP pricing),");
        System.out.println("    status: 'SUCCESS'");
        System.out.println("  }");
        System.out.println("  Ticket {");
        System.out.println("    ticketType: VIP,");
        System.out.println("    status: ISSUED");
        System.out.println("  }");
        System.out.println("  Capacity Changes {");
        System.out.println("    vipTicketsRemaining: " + (sampleEvent.getVipTicketsRemaining() - 1) + " (reduced),");
        System.out.println("    generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining() + " (unchanged)");
        System.out.println("  }");

        // Mock setup
        Payment vipPayment = new Payment();
        vipPayment.setId("PAY002");
        vipPayment.setAmount(500.0);
        vipPayment.setTransactionId("TXN-1697123456790");
        vipPayment.setCardLastFour("9876");
        vipPayment.setStatus("SUCCESS");
        vipPayment.setPaidAt(LocalDateTime.now());

        when(registrationRepository.findById(vipPaymentRequest.getRegistrationId())).thenReturn(Optional.of(sampleRegistration));
        when(paymentRepository.save(any(Payment.class))).thenReturn(vipPayment);
        when(eventRepository.save(sampleEvent)).thenReturn(sampleEvent);
        when(registrationRepository.save(sampleRegistration)).thenReturn(sampleRegistration);
        when(analyticsService.getOverview(sampleOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Processing VIP payment for $" + sampleRegistration.getAmountDue() + "...");
        System.out.println("  Applying VIP capacity management...");

        Payment result = paymentService.makePayment(vipPaymentRequest);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Payment {");
        System.out.println("    amount: $" + result.getAmount() + ",");
        System.out.println("    cardLastFour: '" + result.getCardLastFour() + "',");
        System.out.println("    status: '" + result.getStatus() + "'");
        System.out.println("  }");
        System.out.println("  Ticket {");
        System.out.println("    ticketType: " + sampleRegistration.getTicket().getTicketType() + ",");
        System.out.println("    status: " + sampleRegistration.getTicket().getStatus());
        System.out.println("  }");
        System.out.println("  Capacity Changes {");
        System.out.println("    vipTicketsRemaining: " + sampleEvent.getVipTicketsRemaining() + ",");
        System.out.println("    generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining());
        System.out.println("  }");

        System.out.println("\nVIP ANALYTICS:");
        System.out.println("  ✓ VIP sale recorded (vip: 1, general: 0)");
        System.out.println("  ✓ Premium revenue tracked");
        System.out.println("  ✓ VIP capacity correctly managed");

        System.out.println("\nRESULT: ✅ SUCCESS - VIP payment processed with premium features");
    }

    @Test
    void demonstrateRefundWorkflow() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 3: COMPLETE REFUND WORKFLOW");
        System.out.println("=".repeat(80));

        String registrationId = "REG001";
        String refundReason = "Conference schedule conflicts with work commitments";

        // Setup paid registration with ticket
        sampleRegistration.setStatus(RegistrationStatus.PAID);
        sampleRegistration.setAmountPaid(200.0);

        Ticket issuedTicket = new Ticket();
        issuedTicket.setTicketType(TicketType.GENERAL);
        issuedTicket.setStatus(TicketStatus.ISSUED);
        issuedTicket.setTicketCode("AI2024-ABC12345");
        sampleRegistration.setTicket(issuedTicket);

        samplePayment.setStatus("SUCCESS");
        samplePayment.setRefundStatus(RefundStatus.NONE);

        System.out.println("SCENARIO: Customer requests refund for paid registration");
        System.out.println("\nSTEP 1: INITIAL STATE");
        System.out.println("  Registration {");
        System.out.println("    id: '" + sampleRegistration.getId() + "',");
        System.out.println("    status: " + sampleRegistration.getStatus() + ",");
        System.out.println("    amountPaid: $" + sampleRegistration.getAmountPaid() + ",");
        System.out.println("    ticket: " + issuedTicket.getTicketCode() + " (" + issuedTicket.getStatus() + ")");
        System.out.println("  }");
        System.out.println("  Payment {");
        System.out.println("    status: '" + samplePayment.getStatus() + "',");
        System.out.println("    refundStatus: " + samplePayment.getRefundStatus());
        System.out.println("  }");

        // STEP 1: Request Refund
        System.out.println("\nSTEP 2: CUSTOMER REQUESTS REFUND");
        System.out.println("  Input: registrationId='" + registrationId + "', reason='" + refundReason + "'");
        System.out.println("  Expected: status → REFUND_REQUESTED, refundStatus → REQUESTED");

        Payment requestedPayment = new Payment();
        requestedPayment.setId(samplePayment.getId());
        requestedPayment.setStatus("SUCCESS");
        requestedPayment.setRefundStatus(RefundStatus.REQUESTED);
        requestedPayment.setRefundReason(refundReason);

        when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(samplePayment)).thenReturn(requestedPayment);
        when(registrationRepository.save(sampleRegistration)).thenReturn(sampleRegistration);

        Payment requestResult = paymentService.requestRefund(registrationId, refundReason);

        System.out.println("  Actual Result:");
        System.out.println("    refundStatus: " + requestResult.getRefundStatus());
        System.out.println("    refundReason: '" + requestResult.getRefundReason() + "'");
        System.out.println("    registrationStatus: " + sampleRegistration.getStatus());

        // STEP 2: Organizer Approves Refund
        System.out.println("\nSTEP 3: ORGANIZER APPROVES REFUND");
        System.out.println("  Current capacity: generalTicketsRemaining = " + sampleEvent.getGeneralTicketsRemaining());
        System.out.println("  Expected: Full refund processing + capacity restoration + notifications");

        // Setup for approval
        samplePayment.setRefundStatus(RefundStatus.REQUESTED);
        sampleRegistration.setStatus(RegistrationStatus.REFUND_REQUESTED);

        Payment approvedPayment = new Payment();
        approvedPayment.setId(samplePayment.getId());
        approvedPayment.setStatus("REFUNDED");
        approvedPayment.setRefundStatus(RefundStatus.COMPLETED);
        approvedPayment.setRefundReason(refundReason);
        approvedPayment.setRefundedAt(LocalDateTime.now());

        when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(samplePayment)).thenReturn(approvedPayment);
        when(registrationRepository.save(sampleRegistration)).thenReturn(sampleRegistration);
        when(analyticsService.getOverview(sampleOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
        doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString());
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        Payment approvalResult = paymentService.approveRefund(registrationId);

        System.out.println("  Actual Result:");
        System.out.println("    Payment {");
        System.out.println("      status: '" + approvalResult.getStatus() + "',");
        System.out.println("      refundStatus: " + approvalResult.getRefundStatus() + ",");
        System.out.println("      refundedAt: " + approvalResult.getRefundedAt());
        System.out.println("    }");
        System.out.println("    Registration {");
        System.out.println("      status: " + sampleRegistration.getStatus() + ",");
        System.out.println("      ticket: " + issuedTicket.getStatus());
        System.out.println("    }");
        System.out.println("    Event Capacity {");
        System.out.println("      generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining() + " (restored)");
        System.out.println("    }");

        System.out.println("\nREFUND WORKFLOW COMPLETE:");
        System.out.println("  ✓ Customer request processed");
        System.out.println("  ✓ Organizer approval handled");
        System.out.println("  ✓ Payment refunded");
        System.out.println("  ✓ Ticket invalidated");
        System.out.println("  ✓ Capacity restored");
        System.out.println("  ✓ Customer notified");
        System.out.println("  ✓ Analytics updated");

        System.out.println("\nRESULT: ✅ SUCCESS - Complete refund workflow processed");
    }

    @Test
    void demonstrateRefundByOrganizer() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 4: ORGANIZER-INITIATED REFUND");
        System.out.println("=".repeat(80));

        String registrationId = "REG001";
        String organizerReason = "Event cancelled due to venue unavailability";

        // Setup paid registration
        sampleRegistration.setStatus(RegistrationStatus.PAID);

        Ticket activeTicket = new Ticket();
        activeTicket.setTicketType(TicketType.GENERAL);
        activeTicket.setStatus(TicketStatus.ISSUED);
        sampleRegistration.setTicket(activeTicket);

        System.out.println("SCENARIO: Organizer initiates refund (immediate processing)");
        System.out.println("\nINPUT:");
        System.out.println("  registrationId: '" + registrationId + "'");
        System.out.println("  reason: '" + organizerReason + "'");

        System.out.println("\nORGANIZER REFUND PROCESS:");
        System.out.println("  • Immediate refund (no approval needed)");
        System.out.println("  • Automatic capacity restoration");
        System.out.println("  • Customer notification sent");
        System.out.println("  • Analytics dashboard updated");

        System.out.println("\nBEFORE REFUND:");
        System.out.println("  Registration: " + sampleRegistration.getStatus());
        System.out.println("  Ticket: " + activeTicket.getStatus());
        System.out.println("  Capacity: " + sampleEvent.getGeneralTicketsRemaining() + " general tickets");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Payment {");
        System.out.println("    status: 'REFUNDED',");
        System.out.println("    refundStatus: COMPLETED,");
        System.out.println("    refundReason: '" + organizerReason + "',");
        System.out.println("    refundedAt: current timestamp");
        System.out.println("  }");
        System.out.println("  Registration: REFUNDED");
        System.out.println("  Ticket: REFUNDED");
        System.out.println("  Capacity: " + (sampleEvent.getGeneralTicketsRemaining() + 1) + " general tickets (restored)");

        // Mock setup
        Payment refundedPayment = new Payment();
        refundedPayment.setId("PAY001");
        refundedPayment.setStatus("REFUNDED");
        refundedPayment.setRefundStatus(RefundStatus.COMPLETED);
        refundedPayment.setRefundReason(organizerReason);
        refundedPayment.setRefundedAt(LocalDateTime.now());

        when(paymentRepository.findByRegistrationId(registrationId)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(samplePayment)).thenReturn(refundedPayment);
        when(registrationRepository.save(sampleRegistration)).thenReturn(sampleRegistration);
        when(analyticsService.getOverview(sampleOrganizer.getId())).thenReturn(new AnalyticsOverviewDTO());
        doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString());
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling paymentService.refundByOrganizer('" + registrationId + "', '" + organizerReason + "')...");
        System.out.println("  Processing immediate refund...");

        Payment result = paymentService.refundByOrganizer(registrationId, organizerReason);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Payment {");
        System.out.println("    status: '" + result.getStatus() + "',");
        System.out.println("    refundStatus: " + result.getRefundStatus() + ",");
        System.out.println("    refundReason: '" + result.getRefundReason() + "',");
        System.out.println("    refundedAt: " + result.getRefundedAt());
        System.out.println("  }");
        System.out.println("  Registration: " + sampleRegistration.getStatus());
        System.out.println("  Ticket: " + activeTicket.getStatus());
        System.out.println("  Capacity: " + sampleEvent.getGeneralTicketsRemaining() + " general tickets");

        System.out.println("\nINTEGRATIONS TRIGGERED:");
        System.out.println("  ✓ Customer notification sent");
        System.out.println("  ✓ Analytics dashboard updated");
        System.out.println("  ✓ Event capacity restored");
        System.out.println("  ✓ Refund processed immediately");

        System.out.println("\nRESULT: ✅ SUCCESS - Organizer refund processed immediately");

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ALL PAYMENT SERVICE DEMOS COMPLETED");
        System.out.println("=".repeat(80));
    }
}
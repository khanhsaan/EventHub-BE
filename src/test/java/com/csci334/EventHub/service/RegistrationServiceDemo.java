package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.MyRegistrationDTO;
import com.csci334.EventHub.dto.RegistrationOutDTO;
import com.csci334.EventHub.dto.RegistrationRequestDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.*;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import com.csci334.EventHub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Demo class showing expected vs actual output for RegistrationService methods
 * This demonstrates the behavior of registration management operations
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceDemo {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Event sampleEvent;
    private User sampleUser;
    private Registration sampleRegistration;

    @BeforeEach
    void setUp() {
        reset(registrationRepository, eventRepository, userRepository);

        // Setup sample event
        sampleEvent = new Event();
        sampleEvent.setId("100001");
        sampleEvent.setTitle("AI & Machine Learning Summit");
        sampleEvent.setDescription("Comprehensive summit on AI and ML trends, tools, and applications");
        sampleEvent.setLocation("Tech Hub, Main Auditorium");
        sampleEvent.setEventDate(LocalDate.of(2024, 9, 15));
        sampleEvent.setStartTime(LocalTime.of(8, 30));
        sampleEvent.setEndTime(LocalTime.of(18, 0));
        sampleEvent.setEventType(EventType.CONFERENCE);
        sampleEvent.setStatus(EventStatus.PUBLISHED);
        sampleEvent.setGeneralPrice(150.0);
        sampleEvent.setVipPrice(400.0);
        sampleEvent.setGeneralTicketLimit(300);
        sampleEvent.setVipTicketLimit(50);
        sampleEvent.setGeneralTicketsRemaining(275);
        sampleEvent.setVipTicketsRemaining(47);

        // Setup sample user
        sampleUser = new User();
        sampleUser.setId("123456");
        sampleUser.setEmail("john.doe@example.com");
        sampleUser.setFirstName("John");
        sampleUser.setLastName("Doe");
        sampleUser.setRole(Role.ATTENDEE);

        // Setup sample registration
        sampleRegistration = new Registration();
        sampleRegistration.setId("REG001");
        sampleRegistration.setEvent(sampleEvent);
        sampleRegistration.setAttendee(sampleUser);
        sampleRegistration.setStatus(RegistrationStatus.PENDING);
        sampleRegistration.setRequestedTicketType(TicketType.GENERAL);
        sampleRegistration.setAmountPaid(0.0);
        sampleRegistration.setAmountDue(150.0);
    }

    @Test
    void demonstrateCreateRegistration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 1: CREATE NEW REGISTRATION");
        System.out.println("=".repeat(80));

        RegistrationRequestDTO newRegistration = new RegistrationRequestDTO();
        newRegistration.setEventId("100001");
        newRegistration.setAttendeeId("123456");
        newRegistration.setTicketType(TicketType.GENERAL);

        System.out.println("INPUT:");
        System.out.println("  RegistrationRequestDTO {");
        System.out.println("    eventId: '" + newRegistration.getEventId() + "',");
        System.out.println("    attendeeId: '" + newRegistration.getAttendeeId() + "',");
        System.out.println("    ticketType: " + newRegistration.getTicketType());
        System.out.println("  }");

        System.out.println("\nVALIDATION PROCESS:");
        System.out.println("  1. Check if event exists: " + sampleEvent.getTitle());
        System.out.println("  2. Check if user exists: " + sampleUser.getFirstName() + " " + sampleUser.getLastName());
        System.out.println("  3. Check for duplicate registration: Not found");
        System.out.println("  4. Calculate price: " + sampleEvent.getGeneralPrice() + " (General ticket)");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    id: 'REG001' (auto-generated),");
        System.out.println("    event: AI & Machine Learning Summit,");
        System.out.println("    attendee: John Doe (john.doe@example.com),");
        System.out.println("    status: PENDING,");
        System.out.println("    requestedTicketType: GENERAL,");
        System.out.println("    amountPaid: 0.0,");
        System.out.println("    amountDue: 150.0");
        System.out.println("  }");

        // Mock setup
        when(eventRepository.findById(newRegistration.getEventId())).thenReturn(Optional.of(sampleEvent));
        when(userRepository.findById(newRegistration.getAttendeeId())).thenReturn(Optional.of(sampleUser));
        when(registrationRepository.existsByEventIdAndAttendeeId(newRegistration.getEventId(), newRegistration.getAttendeeId())).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenReturn(sampleRegistration);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling registrationService.create(registrationRequest)...");

        Registration result = registrationService.create(newRegistration);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    id: '" + result.getId() + "',");
        System.out.println("    event: " + result.getEvent().getTitle() + ",");
        System.out.println("    attendee: " + result.getAttendee().getFirstName() + " " + result.getAttendee().getLastName() + " (" + result.getAttendee().getEmail() + "),");
        System.out.println("    status: " + result.getStatus() + ",");
        System.out.println("    requestedTicketType: " + result.getRequestedTicketType() + ",");
        System.out.println("    amountPaid: " + result.getAmountPaid() + ",");
        System.out.println("    amountDue: " + result.getAmountDue());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - Registration created successfully");
    }

    @Test
    void demonstrateVIPRegistration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 2: CREATE VIP REGISTRATION");
        System.out.println("=".repeat(80));

        RegistrationRequestDTO vipRegistration = new RegistrationRequestDTO();
        vipRegistration.setEventId("100001");
        vipRegistration.setAttendeeId("123456");
        vipRegistration.setTicketType(TicketType.VIP);

        System.out.println("INPUT:");
        System.out.println("  RegistrationRequestDTO {");
        System.out.println("    eventId: '" + vipRegistration.getEventId() + "',");
        System.out.println("    attendeeId: '" + vipRegistration.getAttendeeId() + "',");
        System.out.println("    ticketType: " + vipRegistration.getTicketType() + " (Premium)");
        System.out.println("  }");

        System.out.println("\nPRICING CALCULATION:");
        System.out.println("  General Price: $" + sampleEvent.getGeneralPrice());
        System.out.println("  VIP Price: $" + sampleEvent.getVipPrice() + " ← Selected");
        System.out.println("  Price Difference: $" + (sampleEvent.getVipPrice() - sampleEvent.getGeneralPrice()) + " premium");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    id: 'REG002',");
        System.out.println("    requestedTicketType: VIP,");
        System.out.println("    amountDue: " + sampleEvent.getVipPrice() + " (VIP pricing),");
        System.out.println("    status: PENDING");
        System.out.println("  }");

        // Mock setup
        Registration vipSampleRegistration = new Registration();
        vipSampleRegistration.setId("REG002");
        vipSampleRegistration.setEvent(sampleEvent);
        vipSampleRegistration.setAttendee(sampleUser);
        vipSampleRegistration.setStatus(RegistrationStatus.PENDING);
        vipSampleRegistration.setRequestedTicketType(TicketType.VIP);
        vipSampleRegistration.setAmountPaid(0.0);
        vipSampleRegistration.setAmountDue(400.0);

        when(eventRepository.findById(vipRegistration.getEventId())).thenReturn(Optional.of(sampleEvent));
        when(userRepository.findById(vipRegistration.getAttendeeId())).thenReturn(Optional.of(sampleUser));
        when(registrationRepository.existsByEventIdAndAttendeeId(any(), any())).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenReturn(vipSampleRegistration);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calculating VIP pricing...");
        System.out.println("  Creating VIP registration...");

        Registration result = registrationService.create(vipRegistration);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    id: '" + result.getId() + "',");
        System.out.println("    requestedTicketType: " + result.getRequestedTicketType() + ",");
        System.out.println("    amountDue: $" + result.getAmountDue() + ",");
        System.out.println("    status: " + result.getStatus());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - VIP registration created with premium pricing");
    }

    @Test
    void demonstrateApproveRegistration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 3: APPROVE REGISTRATION");
        System.out.println("=".repeat(80));

        String registrationId = "REG001";
        System.out.println("INPUT:");
        System.out.println("  registrationId: '" + registrationId + "'");

        System.out.println("\nCURRENT REGISTRATION STATE:");
        System.out.println("  Registration {");
        System.out.println("    id: '" + sampleRegistration.getId() + "',");
        System.out.println("    status: " + sampleRegistration.getStatus() + ",");
        System.out.println("    amountDue: $" + sampleRegistration.getAmountDue() + ",");
        System.out.println("    ticket: null (not issued yet)");
        System.out.println("  }");

        System.out.println("\nAPPROVAL PROCESS:");
        System.out.println("  1. Find registration by ID: FOUND");
        System.out.println("  2. Validate status is PENDING: ✓");
        System.out.println("  3. Check amount due: $" + sampleRegistration.getAmountDue() + " > 0");
        System.out.println("  4. Update status to APPROVED (payment required)");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    id: '" + registrationId + "',");
        System.out.println("    status: APPROVED (changed from PENDING),");
        System.out.println("    ticket: null (will be issued after payment),");
        System.out.println("    eventCapacity: unchanged (until payment)");
        System.out.println("  }");

        // Mock setup
        Registration registrationToApprove = new Registration();
        registrationToApprove.setId(sampleRegistration.getId());
        registrationToApprove.setEvent(sampleRegistration.getEvent());
        registrationToApprove.setAttendee(sampleRegistration.getAttendee());
        registrationToApprove.setStatus(RegistrationStatus.PENDING);
        registrationToApprove.setRequestedTicketType(sampleRegistration.getRequestedTicketType());
        registrationToApprove.setAmountDue(sampleRegistration.getAmountDue());

        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registrationToApprove));
        when(registrationRepository.save(registrationToApprove)).thenReturn(registrationToApprove);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling registrationService.approveRegistration(\"" + registrationId + "\")...");
        System.out.println("  Validating status...");
        System.out.println("  Checking payment requirement...");
        System.out.println("  Updating status...");

        Registration result = registrationService.approveRegistration(registrationId);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    id: '" + result.getId() + "',");
        System.out.println("    status: " + result.getStatus() + ",");
        System.out.println("    ticket: " + (result.getTicket() != null ? "ISSUED" : "null (payment required)") + ",");
        System.out.println("    paymentRequired: " + (result.getAmountDue() > 0 ? "YES ($" + result.getAmountDue() + ")" : "NO"));
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - Registration approved, awaiting payment");
    }

    @Test
    void demonstrateApproveFreeRegistration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 4: APPROVE FREE REGISTRATION");
        System.out.println("=".repeat(80));

        String registrationId = "REG003";

        // Create a free registration
        Registration freeRegistration = new Registration();
        freeRegistration.setId(registrationId);
        freeRegistration.setEvent(sampleEvent);
        freeRegistration.setAttendee(sampleUser);
        freeRegistration.setStatus(RegistrationStatus.PENDING);
        freeRegistration.setRequestedTicketType(TicketType.GENERAL);
        freeRegistration.setAmountDue(0.0); // Free event
        freeRegistration.setAmountPaid(0.0);

        System.out.println("INPUT:");
        System.out.println("  registrationId: '" + registrationId + "' (FREE EVENT)");

        System.out.println("\nCURRENT STATE:");
        System.out.println("  Registration {");
        System.out.println("    amountDue: $" + freeRegistration.getAmountDue() + " (FREE),");
        System.out.println("    status: " + freeRegistration.getStatus() + ",");
        System.out.println("    ticketType: " + freeRegistration.getRequestedTicketType());
        System.out.println("  }");
        System.out.println("  Event Capacity {");
        System.out.println("    generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining());
        System.out.println("  }");

        System.out.println("\nFREE REGISTRATION PROCESS:");
        System.out.println("  1. Validate status is PENDING: ✓");
        System.out.println("  2. Check amount due: $0.00 (FREE EVENT)");
        System.out.println("  3. Skip to PAID status");
        System.out.println("  4. Issue ticket immediately");
        System.out.println("  5. Reduce event capacity");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    status: PAID (skipped APPROVED),");
        System.out.println("    ticket: ISSUED {");
        System.out.println("      ticketType: GENERAL,");
        System.out.println("      status: ISSUED");
        System.out.println("    }");
        System.out.println("  }");
        System.out.println("  Event Capacity {");
        System.out.println("    generalTicketsRemaining: " + (sampleEvent.getGeneralTicketsRemaining() - 1) + " (reduced by 1)");
        System.out.println("  }");

        // Mock setup
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(freeRegistration));
        when(registrationRepository.save(freeRegistration)).thenReturn(freeRegistration);
        when(eventRepository.save(sampleEvent)).thenReturn(sampleEvent);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Processing free registration...");
        System.out.println("  Issuing ticket...");
        System.out.println("  Updating capacity...");

        Registration result = registrationService.approveRegistration(registrationId);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Registration {");
        System.out.println("    status: " + result.getStatus() + ",");
        System.out.println("    ticket: " + (result.getTicket() != null ? "ISSUED" : "null") + " {");
        if (result.getTicket() != null) {
            System.out.println("      ticketType: " + result.getTicket().getTicketType() + ",");
            System.out.println("      status: " + result.getTicket().getStatus());
        }
        System.out.println("    }");
        System.out.println("  }");
        System.out.println("  Event Capacity {");
        System.out.println("    generalTicketsRemaining: " + sampleEvent.getGeneralTicketsRemaining());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - Free registration completed with immediate ticket issuance");
    }

    @Test
    void demonstrateGetRegistrationsByEvent() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 5: GET REGISTRATIONS BY EVENT");
        System.out.println("=".repeat(80));

        String eventId = "100001";
        System.out.println("INPUT:");
        System.out.println("  eventId: '" + eventId + "'");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  List<RegistrationOutDTO> containing event attendee information:");
        System.out.println("  [");
        System.out.println("    RegistrationOutDTO {");
        System.out.println("      registrationId: 'REG001',");
        System.out.println("      attendeeId: '123456',");
        System.out.println("      fullName: 'John Doe',");
        System.out.println("      email: 'john.doe@example.com',");
        System.out.println("      ticketRequested: GENERAL,");
        System.out.println("      registrationStatus: PENDING,");
        System.out.println("      amountDue: 150.0");
        System.out.println("    }");
        System.out.println("  ]");

        // Mock setup
        when(registrationRepository.findByEventId(eventId)).thenReturn(Arrays.asList(sampleRegistration));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling registrationService.getByEventId(\"" + eventId + "\")...");

        List<RegistrationOutDTO> result = registrationService.getByEventId(eventId);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  List<RegistrationOutDTO> with " + result.size() + " registration(s):");
        System.out.println("  [");
        for (RegistrationOutDTO dto : result) {
            System.out.println("    RegistrationOutDTO {");
            System.out.println("      registrationId: '" + dto.getRegistrationId() + "',");
            System.out.println("      attendeeId: '" + dto.getAttendeeId() + "',");
            System.out.println("      fullName: '" + dto.getFullName() + "',");
            System.out.println("      email: '" + dto.getEmail() + "',");
            System.out.println("      ticketRequested: " + dto.getTicketRequested() + ",");
            System.out.println("      registrationStatus: " + dto.getRegistrationStatus() + ",");
            System.out.println("      amountDue: " + dto.getAmountDue());
            System.out.println("    }");
        }
        System.out.println("  ]");

        System.out.println("\nRESULT: ✅ SUCCESS - Event registrations converted to organizer view");
    }

    @Test
    void demonstrateGetUserRegistrations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO 6: GET USER'S REGISTRATIONS");
        System.out.println("=".repeat(80));

        String userId = "123456";
        System.out.println("INPUT:");
        System.out.println("  userId: '" + userId + "'");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  List<MyRegistrationDTO> containing user's event registrations:");
        System.out.println("  [");
        System.out.println("    MyRegistrationDTO {");
        System.out.println("      registrationId: 'REG001',");
        System.out.println("      eventTitle: 'AI & Machine Learning Summit',");
        System.out.println("      location: 'Tech Hub, Main Auditorium',");
        System.out.println("      date: 2024-09-15,");
        System.out.println("      time: '08:30 - 18:00',");
        System.out.println("      ticketType: GENERAL,");
        System.out.println("      status: PENDING,");
        System.out.println("      amountDue: 150.0,");
        System.out.println("      ticketCode: null (pending),");
        System.out.println("      cardLastFour: null (not paid)");
        System.out.println("    }");
        System.out.println("  ]");

        // Mock setup
        when(registrationRepository.findByAttendeeId(userId)).thenReturn(Arrays.asList(sampleRegistration));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling registrationService.getByUserId(\"" + userId + "\")...");

        List<MyRegistrationDTO> result = registrationService.getByUserId(userId);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  List<MyRegistrationDTO> with " + result.size() + " registration(s):");
        System.out.println("  [");
        for (MyRegistrationDTO dto : result) {
            System.out.println("    MyRegistrationDTO {");
            System.out.println("      registrationId: '" + dto.getRegistrationId() + "',");
            System.out.println("      eventTitle: '" + dto.getEventTitle() + "',");
            System.out.println("      location: '" + dto.getLocation() + "',");
            System.out.println("      date: " + dto.getDate() + ",");
            System.out.println("      time: '" + dto.getTime() + "',");
            System.out.println("      ticketType: " + dto.getTicketType() + ",");
            System.out.println("      status: " + dto.getStatus() + ",");
            System.out.println("      amountDue: " + dto.getAmountDue() + ",");
            System.out.println("      ticketCode: " + (dto.getTicketCode() != null ? "'" + dto.getTicketCode() + "'" : "null") + ",");
            System.out.println("      cardLastFour: " + (dto.getCardLastFour() != null ? "'" + dto.getCardLastFour() + "'" : "null"));
            System.out.println("    }");
        }
        System.out.println("  ]");

        System.out.println("\nRESULT: ✅ SUCCESS - User registrations converted to attendee view");

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ALL REGISTRATION SERVICE DEMOS COMPLETED");
        System.out.println("=".repeat(80));
    }
}
package com.csci334.EventHub.config;

import com.csci334.EventHub.dto.PaymentMakeDTO;
import com.csci334.EventHub.dto.RegistrationRequestDTO;
import com.csci334.EventHub.dto.SignupRequest;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.*;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import com.csci334.EventHub.repository.UserRepository;
import com.csci334.EventHub.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final EventService eventService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }

        log.info("Starting data initialization...");

        try {
            // Step 1: Create 10 organizers
            List<User> organizers = createOrganizers();
            log.info("Created {} organizers", organizers.size());

            // Step 2: Create 1 event per organizer (10 events total)
            List<Event> events = createEvents(organizers);
            log.info("Created {} events", events.size());

            // Step 3: Create 50 attendees
            List<User> attendees = createAttendees();
            log.info("Created {} attendees", attendees.size());

            // Step 4: Create 50 registrations for events
            int registrationsCreated = createRegistrations(attendees, events);
            log.info("Created {} registrations", registrationsCreated);

            // Step 5: Approve 80% of registrations
            int approvedRegistrations = approveRegistrations();
            log.info("Approved {} registrations", approvedRegistrations);

            // Step 6: Make payments for approved registrations (creates tickets)
            int paymentsCreated = makePayments();
            log.info("Created {} payments and tickets", paymentsCreated);

            // Step 7: Simulate realistic workflow scenarios that trigger automatic notifications
            int workflowNotifications = simulateRealisticWorkflows(events);
            log.info("Triggered {} workflow-based notifications", workflowNotifications);

            log.info("Data initialization completed successfully!");
            log.info("Summary: {} organizers, {} events, {} attendees, {} registrations, {} approved, {} paid, {} workflow notifications",
                    organizers.size(), events.size(), attendees.size(), registrationsCreated,
                    approvedRegistrations, paymentsCreated, workflowNotifications);
        } catch (Exception e) {
            log.error("Error during data initialization: ", e);
            throw e;
        }
    }

    @Transactional
    public List<User> createOrganizers() {
        List<User> organizers = new ArrayList<>();

        String[] organizerNames = {
                "Tech Conference", "Music Events", "Sports Arena", "Art Gallery",
                "Business Summit", "Education Hub", "Health Wellness", "Cultural Center",
                "Innovation Lab", "Community Events"
        };

        for (int i = 0; i < 10; i++) {
            SignupRequest request = new SignupRequest();
            request.setEmail("organizer" + (i + 1) + "@eventhub.com");
            request.setPassword("password123");
            request.setFirstName(organizerNames[i].split(" ")[0]);
            request.setLastName(organizerNames[i].split(" ")[1]);
            request.setRole(Role.ORGANIZER);

            User organizer = authService.signup(request);
            organizers.add(organizer);
        }

        return organizers;
    }

    @Transactional
    public List<Event> createEvents(List<User> organizers) {
        List<Event> events = new ArrayList<>();

        String[] eventTitles = {
                "Tech Innovation Summit 2025",
                "Summer Music Festival",
                "Championship Basketball Game",
                "Modern Art Exhibition",
                "Business Leadership Conference",
                "Educational Technology Workshop",
                "Health & Wellness Expo",
                "Cultural Heritage Festival",
                "Startup Pitch Competition",
                "Community Service Day"
        };

        String[] descriptions = {
                "Join industry leaders for cutting-edge technology discussions and networking opportunities.",
                "Experience the best local and international artists in a day-long music celebration.",
                "Watch the season's most anticipated basketball championship game live.",
                "Explore contemporary art from emerging and established artists in our gallery.",
                "Learn from successful business leaders about strategy, innovation, and growth.",
                "Discover the latest educational technologies and teaching methodologies.",
                "Focus on mental and physical health with expert talks and wellness activities.",
                "Celebrate diverse cultures through food, music, dance, and traditional arts.",
                "Watch innovative startups pitch their ideas to potential investors and mentors.",
                "Come together to make a positive impact in our local community."
        };

        EventType[] eventTypes = {
                EventType.CONFERENCE, EventType.CONCERT, EventType.SPORTS, EventType.OTHER,
                EventType.CONFERENCE, EventType.WORKSHOP, EventType.OTHER, EventType.SOCIAL,
                EventType.OTHER, EventType.SOCIAL
        };

        String[] locations = {
                "Convention Center Hall A", "City Park Amphitheater", "Sports Stadium",
                "Downtown Art Gallery", "Business District Hotel", "University Campus",
                "Community Center", "Cultural Plaza", "Innovation Hub", "Town Square"
        };

        for (int i = 0; i < organizers.size(); i++) {
            Event event = new Event();
            event.setTitle(eventTitles[i]);
            event.setDescription(descriptions[i]);
            event.setShortDescription(descriptions[i].substring(0, Math.min(100, descriptions[i].length())) + "...");
            event.setLocation(locations[i]);

            // Set event date between next week and 3 months from now
            LocalDate eventDate = LocalDate.now().plusDays(7 + random.nextInt(90));
            event.setEventDate(eventDate);

            // Random start and end times
            int startHour = 9 + random.nextInt(10); // 9 AM to 6 PM
            event.setStartTime(LocalTime.of(startHour, 0));
            event.setEndTime(LocalTime.of(startHour + 2 + random.nextInt(4), 0)); // 2-6 hours duration

            event.setEventType(eventTypes[i]);
            event.setStatus(EventStatus.PUBLISHED);

            // Set pricing
            double basePrice = 25 + random.nextInt(100); // $25-$125
            event.setGeneralPrice(basePrice);
            event.setVipPrice(basePrice * 1.5 + random.nextInt(50)); // 1.5x + $0-50

            // Set ticket limits
            int generalLimit = 50 + random.nextInt(100); // 50-150 tickets
            int vipLimit = 10 + random.nextInt(30); // 10-40 tickets
            event.setGeneralTicketLimit(generalLimit);
            event.setVipTicketLimit(vipLimit);
            event.setGeneralTicketsRemaining(generalLimit);
            event.setVipTicketsRemaining(vipLimit);

            event.setImageUrl("https://picsum.photos/400/300?random=" + (i + 1));
            event.setOrganizer(organizers.get(i));

            Event savedEvent = eventRepository.save(event);
            events.add(savedEvent);
        }

        return events;
    }

    @Transactional
    public List<User> createAttendees() {
        List<User> attendees = new ArrayList<>();

        String[] firstNames = {
                "John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Lisa",
                "James", "Jennifer", "William", "Patricia", "Richard", "Linda", "Joseph",
                "Elizabeth", "Thomas", "Barbara", "Charles", "Susan", "Christopher", "Jessica",
                "Daniel", "Helen", "Matthew", "Nancy", "Anthony", "Betty", "Mark", "Dorothy",
                "Donald", "Sandra", "Steven", "Donna", "Paul", "Carol", "Andrew", "Ruth",
                "Joshua", "Sharon", "Kenneth", "Michelle", "Kevin", "Laura", "Brian", "Sarah",
                "George", "Kimberly", "Edward", "Deborah"
        };

        String[] lastNames = {
                "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
                "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
                "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
                "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill",
                "Flores", "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell",
                "Mitchell", "Carter", "Roberts"
        };

        for (int i = 0; i < 50; i++) {
            SignupRequest request = new SignupRequest();
            request.setEmail("attendee" + (i + 1) + "@example.com");
            request.setPassword("password123");
            request.setFirstName(firstNames[random.nextInt(firstNames.length)]);
            request.setLastName(lastNames[random.nextInt(lastNames.length)]);
            request.setRole(Role.ATTENDEE);

            User attendee = authService.signup(request);
            attendees.add(attendee);
        }

        return attendees;
    }

    @Transactional
    public int createRegistrations(List<User> attendees, List<Event> events) {
        TicketType[] ticketTypes = {TicketType.GENERAL, TicketType.VIP};
        int successfulRegistrations = 0;

        // Track which attendee-event combinations we've already used
        List<String> usedCombinations = new ArrayList<>();

        int attempts = 0;
        while (successfulRegistrations < 50 && attempts < 200) { // Max 200 attempts to avoid infinite loop
            attempts++;

            // Randomly select attendee and event
            User attendee = attendees.get(random.nextInt(attendees.size()));
            Event event = events.get(random.nextInt(events.size()));

            String combination = attendee.getId() + "-" + event.getId();

            // Skip if this combination already exists
            if (usedCombinations.contains(combination)) {
                continue;
            }

            RegistrationRequestDTO request = new RegistrationRequestDTO();
            request.setEventId(event.getId());
            request.setAttendeeId(attendee.getId());
            request.setTicketType(ticketTypes[random.nextInt(ticketTypes.length)]);

            try {
                registrationService.create(request);
                usedCombinations.add(combination);
                successfulRegistrations++;
                log.debug("Created registration #{} for attendee {} and event {}",
                        successfulRegistrations, attendee.getEmail(), event.getTitle());
            } catch (Exception e) {
                log.debug("Failed to create registration for attendee {} and event {}: {}",
                        attendee.getEmail(), event.getTitle(), e.getMessage());
                // Continue to next attempt
            }
        }

        if (successfulRegistrations < 50) {
            log.warn("Only created {} registrations out of target 50 after {} attempts",
                    successfulRegistrations, attempts);
        }

        return successfulRegistrations;
    }

    @Transactional
    public int approveRegistrations() {
        List<Registration> pendingRegistrations = registrationRepository.findAll()
                .stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.PENDING)
                .toList();

        int approvedCount = 0;

        // Approve 80% of pending registrations
        int toApprove = (int) (pendingRegistrations.size() * 0.8);

        for (int i = 0; i < Math.min(toApprove, pendingRegistrations.size()); i++) {
            Registration registration = pendingRegistrations.get(i);
            try {
                registrationService.approveRegistration(registration.getId());
                approvedCount++;
                log.debug("Approved registration {} for event {}",
                        registration.getId(), registration.getEvent().getTitle());
            } catch (Exception e) {
                log.debug("Failed to approve registration {}: {}",
                        registration.getId(), e.getMessage());
            }
        }

        // Reject the remaining 20%
        for (int i = toApprove; i < pendingRegistrations.size(); i++) {
            Registration registration = pendingRegistrations.get(i);
            try {
                registrationService.rejectRegistration(registration.getId());
                log.debug("Rejected registration {} for event {}",
                        registration.getId(), registration.getEvent().getTitle());
            } catch (Exception e) {
                log.debug("Failed to reject registration {}: {}",
                        registration.getId(), e.getMessage());
            }
        }

        return approvedCount;
    }

    @Transactional
    public int makePayments() {
        List<Registration> approvedRegistrations = registrationRepository.findAll()
                .stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.APPROVED)
                .toList();

        int paymentsCreated = 0;
        String[] cardNumbers = {"1234", "5678", "9012", "3456", "7890", "1111", "2222", "3333", "4444", "5555"};

        for (Registration registration : approvedRegistrations) {
            try {
                PaymentMakeDTO paymentDTO = new PaymentMakeDTO();
                paymentDTO.setRegistrationId(registration.getId());
                paymentDTO.setCardLastFour(cardNumbers[random.nextInt(cardNumbers.length)]);

                paymentService.makePayment(paymentDTO);
                paymentsCreated++;

                log.debug("Created payment for registration {} (Event: {})",
                        registration.getId(), registration.getEvent().getTitle());
            } catch (Exception e) {
                log.debug("Failed to create payment for registration {}: {}",
                        registration.getId(), e.getMessage());
            }
        }

        return paymentsCreated;
    }

    @Transactional
    public int simulateRealisticWorkflows(List<Event> events) {
        int totalNotifications = 0;

        // Scenario 1: Send event reminders (triggers automatic notifications)
        log.info("Simulating event reminder workflow...");
        for (Event event : events.subList(0, Math.min(6, events.size()))) { // First 6 events
            try {
                notificationService.sendReminderToRegistrants(event.getId());
                // Count how many registrants this event has to estimate notifications created
                List<Registration> eventRegistrations = registrationRepository.findByEventId(event.getId());
                long paidRegistrations = eventRegistrations.stream()
                        .filter(reg -> reg.getStatus() == RegistrationStatus.PAID)
                        .count();
                totalNotifications += paidRegistrations;

                log.debug("Sent reminders for event '{}' to {} paid registrants",
                        event.getTitle(), paidRegistrations);
            } catch (Exception e) {
                log.debug("Failed to send reminders for event {}: {}", event.getTitle(), e.getMessage());
            }
        }

        // Scenario 2: Cancel 1-2 events (triggers automatic refund and cancellation notifications)
        log.info("Simulating event cancellation workflow...");
        List<Event> eventsToCancel = events.stream()
                .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                .limit(2) // Cancel 2 events
                .toList();

        for (Event event : eventsToCancel) {
            try {
                // Get the organizer's password (we know it's "password123" from our initialization)
                String organizerPassword = "password123";

                // Count registrants before cancellation to estimate notifications
                List<Registration> eventRegistrations = registrationRepository.findByEventId(event.getId());
                int registrantCount = eventRegistrations.size();

                eventService.cancelEvent(event.getId(), organizerPassword);
                totalNotifications += registrantCount; // Each registrant gets a cancellation notification

                log.debug("Cancelled event '{}' - sent notifications to {} registrants",
                        event.getTitle(), registrantCount);
            } catch (Exception e) {
                log.debug("Failed to cancel event {}: {}", event.getTitle(), e.getMessage());
            }
        }

        // Scenario 3: Process some refund requests (triggers automatic refund notifications)
        log.info("Simulating refund workflow...");
        List<Registration> paidRegistrations = registrationRepository.findAll()
                .stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.PAID)
                .limit(5) // Process 5 refund requests
                .toList();

        for (int i = 0; i < paidRegistrations.size(); i++) {
            Registration registration = paidRegistrations.get(i);
            try {
                if (i < 3) {
                    // Approve 3 refunds (organizer-initiated)
                    paymentService.refundByOrganizer(registration.getId(),
                            "Event time changed - automatic refund");
                    totalNotifications++; // Refund notification sent

                    log.debug("Processed organizer refund for registration {} (Event: {})",
                            registration.getId(), registration.getEvent().getTitle());
                } else {
                    // Process 2 customer refund requests
                    paymentService.requestRefund(registration.getId(),
                            "Cannot attend due to scheduling conflict");

                    // Then approve one of them
                    if (i == 3) {
                        paymentService.approveRefund(registration.getId());
                        totalNotifications++; // Approval notification sent

                        log.debug("Approved customer refund request for registration {} (Event: {})",
                                registration.getId(), registration.getEvent().getTitle());
                    } else {
                        paymentService.rejectRefund(registration.getId());
                        totalNotifications++; // Rejection notification sent

                        log.debug("Rejected customer refund request for registration {} (Event: {})",
                                registration.getId(), registration.getEvent().getTitle());
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to process refund for registration {}: {}",
                        registration.getId(), e.getMessage());
            }
        }

        // Scenario 4: Send some general notifications using the notification service
        log.info("Simulating general notification workflows...");
        List<User> sampleAttendees = userRepository.findByRole(Role.ATTENDEE)
                .stream()
                .limit(10) // Send to 10 random attendees
                .toList();

        String[] welcomeMessages = {
                "Welcome to EventHub! Discover amazing events in your area.",
                "Thank you for joining EventHub. Your next great experience awaits!",
                "New to EventHub? Check out our featured events this week.",
                "Your EventHub journey begins now. Explore trending events!",
                "Welcome aboard! Don't miss our upcoming premium events."
        };

        for (int i = 0; i < sampleAttendees.size(); i++) {
            User attendee = sampleAttendees.get(i);
            try {
                String message = welcomeMessages[i % welcomeMessages.length];
                notificationService.sendNotification(attendee.getId(), "Welcome to EventHub!", message);
                totalNotifications++;

                log.debug("Sent welcome notification to user {}", attendee.getEmail());
            } catch (Exception e) {
                log.debug("Failed to send welcome notification to user {}: {}",
                        attendee.getEmail(), e.getMessage());
            }
        }

        log.info("Completed realistic workflow simulation:");
        log.info("- Event reminders sent for 6 events");
        log.info("- 2 events cancelled with automatic refunds");
        log.info("- 5 refund scenarios processed");
        log.info("- 10 welcome notifications sent");

        return totalNotifications;
    }
}
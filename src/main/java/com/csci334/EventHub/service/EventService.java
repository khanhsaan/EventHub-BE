package com.csci334.EventHub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
// Add necessary imports
import org.springframework.security.authentication.BadCredentialsException; // Import standard Spring Security exception
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csci334.EventHub.dto.EventCreationDTO;
import com.csci334.EventHub.dto.EventEditDTO;
import com.csci334.EventHub.dto.EventOutDTO; // Import the new DTO
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Notification;
import com.csci334.EventHub.entity.Payment;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.EventStatus;
import com.csci334.EventHub.entity.enums.EventType;
import com.csci334.EventHub.entity.enums.RefundStatus;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketStatus;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.NotificationRepository;
import com.csci334.EventHub.repository.PaymentRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import com.csci334.EventHub.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException; // Import standard JPA exception

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private final SimpMessagingTemplate messaging;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
            RegistrationRepository registrationRepository, PaymentRepository paymentRepository,
            NotificationRepository notificationRepository, NotificationService notificationService,
            PasswordEncoder passwordEncoder, SimpMessagingTemplate messagingTemplate) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.notificationRepository = notificationRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.messaging = messagingTemplate;
    }

    public List<EventOutDTO> getAll() {
        return eventRepository.findAll().stream().map(event -> {
            EventOutDTO dto = new EventOutDTO();
            dto.setId(event.getId());
            dto.setTitle(event.getTitle());
            dto.setDescription(event.getDescription());
            dto.setShortDescription(event.getShortDescription());
            dto.setLocation(event.getLocation());
            dto.setEventDate(event.getEventDate());
            dto.setStartTime(event.getStartTime());
            dto.setEndTime(event.getEndTime());
            dto.setLastUpdatedAt(event.getLastUpdatedAt());
            dto.setEventType(event.getEventType());
            dto.setStatus(event.getStatus());
            dto.setGeneralPrice(event.getGeneralPrice());
            dto.setVipPrice(event.getVipPrice());
            dto.setGeneralTicketLimit(event.getGeneralTicketLimit());
            dto.setVipTicketLimit(event.getVipTicketLimit());
            dto.setGeneralTicketsRemaining(event.getGeneralTicketsRemaining());
            dto.setVipTicketsRemaining(event.getVipTicketsRemaining());
            dto.setImageUrl(event.getImageUrl());
            if (event.getOrganizer() != null) {
                dto.setOrganizerId(event.getOrganizer().getId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public Optional<Event> getById(String id) {
        return eventRepository.findById(id);
    }

    public List<Event> getByType(EventType type) {
        return eventRepository.findByEventType(type);
    }

    public List<Event> getByOrganizer(String organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    public List<Event> getUpcoming() {
        return eventRepository.findByEventDateAfter(LocalDateTime.now());
    }

    public List<Event> searchByTitle(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Transactional
    public Event create(EventCreationDTO eventDTO) {
        // Create a new event from the DTO
        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setShortDescription(eventDTO.getShortDescription());
        event.setLocation(eventDTO.getLocation());
        event.setEventDate(eventDTO.getEventDate());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setEventType(eventDTO.getEventType());
        event.setStatus(eventDTO.getStatus() != null ? eventDTO.getStatus() : EventStatus.PUBLISHED);
        event.setGeneralPrice(eventDTO.getGeneralPrice());
        event.setVipPrice(eventDTO.getVipPrice());
        event.setGeneralTicketLimit(eventDTO.getGeneralTicketLimit());
        event.setVipTicketLimit(eventDTO.getVipTicketLimit());
        event.setImageUrl(eventDTO.getImageUrl());

        // Find the organizer by ID and set it
        if (eventDTO.getOrganizerId() != null) {
            User organizer = userRepository.findById(eventDTO.getOrganizerId())
                    .orElseThrow(
                            // Replace UserNotFoundException with EntityNotFoundException
                            () -> new EntityNotFoundException(
                                    "Organizer not found with ID: " + eventDTO.getOrganizerId()));
            event.setOrganizer(organizer);
        } else {
            throw new IllegalArgumentException("Organizer ID cannot be null when creating an event.");
        }

        Event saved = eventRepository.save(event);

        // Send to /topic/events/published
        messaging.convertAndSend("/topic/events/published", List.of(saved));

        return saved;

    }

    @Transactional
    public Event update(String id, EventEditDTO dto) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));

        existingEvent.setTitle(dto.getTitle());
        existingEvent.setShortDescription(dto.getShortDescription());
        existingEvent.setDescription(dto.getDescription());
        existingEvent.setLocation(dto.getLocation());
        existingEvent.setEventDate(dto.getEventDate());
        existingEvent.setStartTime(dto.getStartTime());
        existingEvent.setEndTime(dto.getEndTime());
        existingEvent.setEventType(dto.getEventType());
        existingEvent.setImageUrl(dto.getImageUrl());

        existingEvent.setGeneralPrice(dto.getGeneralPrice());
        existingEvent.setVipPrice(dto.getVipPrice());
        existingEvent.setGeneralTicketLimit(dto.getGeneralTicketLimit());
        existingEvent.setVipTicketLimit(dto.getVipTicketLimit());
        existingEvent.setGeneralTicketsRemaining(dto.getGeneralTicketsRemaining());
        existingEvent.setVipTicketsRemaining(dto.getVipTicketsRemaining());

        // ✅ Keep the existing status instead of setting it to null
        // No change needed here if you're modifying in-place

        Event saved = eventRepository.save(existingEvent);

        // Send WebSocket notifications based on existing status
        if (saved.getStatus() == EventStatus.PUBLISHED) {
            messaging.convertAndSend("/topic/events/published", List.of(saved));
        } else if (saved.getStatus() == EventStatus.IN_PROGRESS) {
            messaging.convertAndSend("/topic/events/inprogress", List.of(saved));
        }

        return saved;
    }

    @Transactional
    public void delete(String id) {
        eventRepository.deleteById(id);
    }

    // --- Cancel Event Method using standard exceptions ---
    @Transactional
    public Event cancelEvent(String eventId, String password) {
        // 1. Find the event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // 2. Get the organizer
        User organizer = event.getOrganizer();
        if (organizer == null) {
            throw new IllegalStateException("Event with ID " + eventId + " does not have an associated organizer.");
        }

        // 3. Verify the password
        if (!passwordEncoder.matches(password, organizer.getPassword())) {
            throw new BadCredentialsException("Incorrect password provided for cancellation.");
        }

        // 4. Check if already cancelled or completed
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            System.out.println("Event " + eventId + " is already " + event.getStatus() + ". No action taken.");
            return event;
        }

        // 5. Cancel the event
        event.setStatus(EventStatus.CANCELLED);
        event.setLastUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);

        // 6. Refund all eligible paid registrations
        List<Registration> registrations = registrationRepository.findByEventId(eventId);
        for (Registration reg : registrations) {
            if (reg.getStatus() == RegistrationStatus.PAID && reg.getAmountDue() > 0) {
                Payment payment = paymentRepository.findByRegistrationId(reg.getId()).orElse(null);
                if (payment != null && payment.getStatus().equals("SUCCESS")) {
                    payment.setStatus("REFUNDED");
                    payment.setRefundStatus(RefundStatus.COMPLETED);
                    payment.setRefundReason("Event cancelled by organizer");
                    payment.setRefundedAt(LocalDateTime.now());

                    reg.setStatus(RegistrationStatus.REFUNDED);
                    reg.getTicket().setStatus(TicketStatus.REFUNDED);

                    paymentRepository.save(payment);
                    registrationRepository.save(reg);
                }
            } else {
                reg.setStatus(RegistrationStatus.CANCELLED);
                reg.getTicket().setStatus(TicketStatus.EXPIRED);
                registrationRepository.save(reg);
            }

            User recipient = reg.getAttendee();

            Notification notification = new Notification();
            notification.setTitle("Event Cancelled: " + event.getTitle());
            notification.setMessage("The event '" + event.getTitle() + "' scheduled on " + event.getEventDate()
                    + " has been cancelled.");
            notification.setRecipient(recipient);
            notification.setEvent(event);
            notification.setSentAt(LocalDateTime.now());
            notification.setRead(false);

            notificationRepository.save(notification);

            messaging.convertAndSend("/topic/notifications/" + recipient.getId(),
                    notificationService.convertToDto(notification));

        }

        // 7. Notify subscribers
        messaging.convertAndSend("/topic/events/cancelled", List.of(event));

        return event;
    }

    // --- End Cancel Event Method ---

    @Scheduled(cron = "0 * * * * *") // every 5 minute
    @Transactional
    public void updateInProgressEventStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> published = eventRepository.findByStatus(EventStatus.PUBLISHED);
        List<Event> toStart = published.stream()
                .filter(e -> {
                    LocalDateTime startAt = LocalDateTime.of(e.getEventDate(), e.getStartTime());
                    LocalDateTime endAt = LocalDateTime.of(e.getEventDate(), e.getEndTime());
                    return !startAt.isAfter(now) && endAt.isAfter(now);
                })
                .peek(e -> {
                    e.setStatus(EventStatus.IN_PROGRESS);
                    e.setLastUpdatedAt(now);
                })
                .collect(Collectors.toList());

        if (!toStart.isEmpty()) {
            // Persist the changes
            eventRepository.saveAll(toStart);
            // Notify subscribed clients
            messaging.convertAndSend(
                    "/topic/events/inprogress",
                    toStart);
        }

        log.info("→ running updateInProgressEventStatuses at {}", LocalDateTime.now());
    }

    @Scheduled(cron = "0 * * * * *") // every 5 minute
    @Transactional
    public void updatePastEventStatuses() {
        LocalDateTime now = LocalDateTime.now();
        // Get both PUBLISHED and IN_PROGRESS events
        List<Event> activeEvents = new ArrayList<>();
        activeEvents.addAll(eventRepository.findByStatus(EventStatus.PUBLISHED));
        activeEvents.addAll(eventRepository.findByStatus(EventStatus.IN_PROGRESS));

        List<Event> toComplete = activeEvents.stream()
                .filter(e -> LocalDateTime.of(e.getEventDate(), e.getEndTime()).isBefore(now))
                .peek(e -> {
                    e.setStatus(EventStatus.COMPLETED);
                    e.setLastUpdatedAt(now);
                })
                .collect(Collectors.toList());

        if (!toComplete.isEmpty()) {
            eventRepository.saveAll(toComplete);
            messaging.convertAndSend(
                    "/topic/events/completed",
                    toComplete);
        }
    }
}
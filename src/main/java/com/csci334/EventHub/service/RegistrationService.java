package com.csci334.EventHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.Ticket;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketStatus;
import com.csci334.EventHub.entity.enums.TicketType;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import com.csci334.EventHub.repository.UserRepository; // Import UserRepository
import com.csci334.EventHub.dto.MyRegistrationDTO;
import com.csci334.EventHub.dto.RegistrationOutDTO;
import com.csci334.EventHub.dto.RegistrationRequestDTO; // Import RegistrationRequestDTO
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RegistrationService {
    private final RegistrationRepository repo;
    private final EventRepository eventRepo;
    private final UserRepository userRepo;

    public RegistrationService(RegistrationRepository repo, EventRepository eventRepo, UserRepository userRepo) {
        this.repo = repo;
        this.eventRepo = eventRepo;
        this.userRepo = userRepo; // Initialize UserRepository
    }

    public List<Registration> getAll() {
        return repo.findAll();
    }

    @Transactional
    public Registration create(RegistrationRequestDTO dto) {
        Event event = eventRepo.findById(dto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        User attendee = userRepo.findById(dto.getAttendeeId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (repo.existsByEventIdAndAttendeeId(dto.getEventId(), dto.getAttendeeId())) {
            throw new IllegalArgumentException("You have already registered for this event.");
        }

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setAttendee(attendee);
        registration.setAmountPaid(0.0);
        registration.setStatus(RegistrationStatus.PENDING); // set to PENDING initially

        double price = dto.getTicketType() == TicketType.VIP ? event.getVipPrice() : event.getGeneralPrice();
        registration.setAmountDue(price);
        registration.setRequestedTicketType(dto.getTicketType());

        return repo.save(registration);
    }

    public List<RegistrationOutDTO> getByEventId(String eventId) {
        List<Registration> registrations = repo.findByEventId(eventId);
        List<RegistrationOutDTO> dtos = new ArrayList<>();
        for (Registration registration : registrations) {
            RegistrationOutDTO dto = new RegistrationOutDTO();
            dto.setRegistrationId(registration.getId());
            dto.setAttendeeId(registration.getAttendee().getId());
            dto.setEmail(registration.getAttendee().getEmail());
            dto.setFullName(registration.getAttendee().getFullName());
            dto.setTicketRequested(registration.getRequestedTicketType());
            dto.setRegistrationStatus(registration.getStatus());
            dto.setAmountDue(registration.getAmountDue());
            dtos.add(dto);
        }

        return dtos;
    }

    @Transactional
    public Registration approveRegistration(String registrationId) {
        Registration registration = repo.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found"));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING registrations can be approved.");
        }

        registration.setStatus(RegistrationStatus.APPROVED);

        if (registration.getAmountDue() == 0.0) {
            registration.setStatus(RegistrationStatus.PAID);

            Ticket ticket = new Ticket();
            ticket.setTicketType(registration.getRequestedTicketType());
            ticket.setStatus(TicketStatus.ISSUED);
            ticket.setRegistration(registration);
            registration.setTicket(ticket);

            // Adjust event capacity
            Event event = registration.getEvent();
            if (registration.getRequestedTicketType() == TicketType.VIP) {
                event.setVipTicketsRemaining(event.getVipTicketsRemaining() - 1);
            } else {
                event.setGeneralTicketsRemaining(event.getGeneralTicketsRemaining() - 1);
            }

            eventRepo.save(event);
        }

        return repo.save(registration);
    }

    @Transactional
    public Registration rejectRegistration(String registrationId) {
        Registration registration = repo.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found"));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING registrations can be rejected.");
        }

        registration.setStatus(RegistrationStatus.REJECTED);
        return repo.save(registration);
    }

    public List<MyRegistrationDTO> getByUserId(String userId) {
        List<Registration> registrations = repo.findByAttendeeId(userId);
        List<MyRegistrationDTO> dtos = new ArrayList<>();
        for (Registration registration : registrations) {
            MyRegistrationDTO dto = new MyRegistrationDTO();
            dto.setRegistrationId(registration.getId());
            dto.setAttendeeId(registration.getAttendee().getId());
            dto.setEventTitle(registration.getEvent().getTitle());
            dto.setLocation(registration.getEvent().getLocation());
            dto.setDate(registration.getEvent().getEventDate());
            dto.setTime(registration.getEvent().getStartTime().toString() + " - "
                    + registration.getEvent().getEndTime().toString());
            dto.setTicketType(registration.getRequestedTicketType());
            dto.setStatus(registration.getStatus());
            dto.setAmountDue(registration.getAmountDue());
            if (registration.getTicket() != null) {
                dto.setTicketCode(registration.getTicket().getTicketCode());
            }
            if (registration.getPayment() != null) {
                dto.setCardLastFour(registration.getPayment().getCardLastFour());
            }

            dtos.add(dto);
        }

        return dtos;
    }

}

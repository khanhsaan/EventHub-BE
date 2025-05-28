package com.csci334.EventHub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.service.RegistrationService;
import com.csci334.EventHub.dto.EventTicketsDTO;
import com.csci334.EventHub.dto.MyRegistrationDTO;
import com.csci334.EventHub.dto.RegistrationAttendeeDTO;
import com.csci334.EventHub.dto.RegistrationOutDTO;
import com.csci334.EventHub.dto.RegistrationRequestDTO; // Add import
import com.csci334.EventHub.entity.Ticket; // Add import
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.Event;
import java.util.stream.Collectors; // Add import
import java.util.ArrayList; // Add import

import java.net.URI;
import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService svc;

    public RegistrationController(RegistrationService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<Registration> all() {
        return svc.getAll();
    }

    @PostMapping
    public ResponseEntity<Registration> create(@RequestBody RegistrationRequestDTO dto) {
        Registration created = svc.create(dto);
        return ResponseEntity.created(URI.create("/api/registrations/" + created.getId()))
                .body(created);
    }

    @GetMapping("/events/{eventId}")
    public List<RegistrationOutDTO> getRegistrationsByEvent(@PathVariable String eventId) {
        return svc.getByEventId(eventId);
    }

    @GetMapping("/users/{userId}")
    public List<MyRegistrationDTO> getRegistrationsByUserId(@PathVariable String userId) {
        return svc.getByUserId(userId);
    }

    @PutMapping("/{registrationId}/approve")
    public ResponseEntity<Registration> approveRegistration(@PathVariable String registrationId) {
        Registration updated = svc.approveRegistration(registrationId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{registrationId}/reject")
    public ResponseEntity<Registration> rejectRegistration(@PathVariable String registrationId) {
        Registration updated = svc.rejectRegistration(registrationId);
        return ResponseEntity.ok(updated);
    }

}

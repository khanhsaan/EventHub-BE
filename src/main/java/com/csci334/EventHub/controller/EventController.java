package com.csci334.EventHub.controller;

// Add necessary imports
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import com.csci334.EventHub.dto.EventCancelRequestDTO;
import com.csci334.EventHub.dto.EventCreationDTO;
import com.csci334.EventHub.dto.EventOutDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.enums.EventType;
import com.csci334.EventHub.service.EventService;

import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Map; // For simple error response

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService svc;

    public EventController(EventService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<EventOutDTO> all() {
        return svc.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> one(@PathVariable String id) {
        return svc.getById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public List<Event> byType(@PathVariable EventType type) {
        return svc.getByType(type);
    }

    @GetMapping("/organizer/{organizerId}")
    public List<Event> byOrganizer(@PathVariable String organizerId) {
        return svc.getByOrganizer(organizerId);
    }

    @GetMapping("/upcoming")
    public List<Event> upcoming() {
        return svc.getUpcoming();
    }

    @GetMapping("/search")
    public List<Event> search(@RequestParam String keyword) {
        return svc.searchByTitle(keyword);
    }

    @PostMapping
    public ResponseEntity<Event> create(@RequestBody EventCreationDTO event) {

        Event created = svc.create(event);
        System.out.println("In controller called");
        return ResponseEntity.created(URI.create("/api/events/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable String id,
            @RequestBody Event event) {
        // Consider using a DTO here as well for updates
        return ResponseEntity.ok(svc.update(id, event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- New Cancel Event Endpoint ---
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelEvent(@PathVariable String id, @RequestBody EventCancelRequestDTO cancelRequest) {
        try {
            Event cancelledEvent = svc.cancelEvent(id, cancelRequest.getPassword());
            return ResponseEntity.ok(cancelledEvent);
        } catch (EntityNotFoundException e) {
            // Return 404 Not Found if event doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (BadCredentialsException e) {
            // Return 401 Unauthorized for incorrect password
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            // Return 409 Conflict for state issues (e.g., no organizer, already cancelled)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Generic error handler for other unexpected issues
            // Log the exception e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred during cancellation."));
        }
    }
    // --- End New Cancel Event Endpoint ---
}

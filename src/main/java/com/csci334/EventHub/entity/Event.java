package com.csci334.EventHub.entity;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

import com.csci334.EventHub.entity.enums.EventStatus;
import com.csci334.EventHub.entity.enums.EventType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Import this

@Entity
@Data
public class Event {
    @Id
    @Column(length = 6, updatable = false)
    private String id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
    private String shortDescription;
    private String imageUrl;

    private LocalDate eventDate;
    private String location;

    // Added start and end time fields
    private LocalTime startTime;
    private LocalTime endTime;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    @Enumerated(EnumType.STRING)
    private EventType eventType;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private EventStatus status;

    private Double generalPrice;
    private Double vipPrice;

    // NEW: ticket capacities
    private Integer generalTicketLimit;
    private Integer vipTicketLimit;

    // NEW: how many left
    private Integer generalTicketsRemaining;
    private Integer vipTicketsRemaining;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", updatable = false)
    @JsonBackReference(value = "user-event")
    private User organizer;

    @OneToMany(mappedBy = "event")
    @JsonManagedReference(value = "event-registration")
    private List<Registration> registrations;

    private static final SecureRandom RANDOM = new SecureRandom();

    @PrePersist
    protected void onCreate() {
        // assign a 6‑digit random ID if not already set
        if (this.id == null) {
            this.id = String.format("%06d", RANDOM.nextInt(1_000_000));
        }
        // set creation timestamp
        this.createdAt = LocalDateTime.now();
        // initialize lastUpdatedAt to now as well
        this.lastUpdatedAt = this.createdAt;

        if (this.generalTicketsRemaining == null && this.generalTicketLimit != null) {
            this.generalTicketsRemaining = this.generalTicketLimit;
        }
        if (this.vipTicketsRemaining == null && this.vipTicketLimit != null) {
            this.vipTicketsRemaining = this.vipTicketLimit;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}

package com.csci334.EventHub.entity;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

import com.csci334.EventHub.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
public class User {
    @Id
    @Column(length = 6)
    private String id;

    private String email;
    private String password;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "user-event")
    private List<Event> organizedEvents;

    @OneToMany(mappedBy = "attendee")
    @JsonManagedReference(value = "user-registration")
    private List<Registration> registrations;

    @OneToMany(mappedBy = "recipient")
    private List<Notification> notifications;

    private LocalDateTime createdAt;

    // random generator for IDs
    private static final SecureRandom RANDOM = new SecureRandom();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @PrePersist
    protected void onCreate() {
        // assign a 6‑digit ID if not set
        if (this.id == null) {
            this.id = String.format("%06d", RANDOM.nextInt(1_000_000));
        }
        // set creation timestamp
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

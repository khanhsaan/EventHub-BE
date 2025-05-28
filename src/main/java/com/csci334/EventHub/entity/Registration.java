package com.csci334.EventHub.entity;

import java.security.SecureRandom;

import jakarta.persistence.*;
import lombok.Data;

import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "event_id", "attendee_id" })
})
@Data
public class Registration {
    @Id
    @Column(length = 6, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", updatable = false)
    @JsonBackReference(value = "event-registration")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendee_id", updatable = false)
    @JsonBackReference(value = "user-registration")
    private User attendee;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    private Double amountPaid;
    private Double amountDue;

    @OneToOne(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference(value = "registration-payment")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private TicketType requestedTicketType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ticket_id", nullable = true)
    @JsonManagedReference(value = "registration-ticket")
    private Ticket ticket;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        }
        if (this.status == null) {
            this.status = RegistrationStatus.PENDING;
        }
    }
}

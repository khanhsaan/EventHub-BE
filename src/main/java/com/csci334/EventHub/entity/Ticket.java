package com.csci334.EventHub.entity;

import com.csci334.EventHub.entity.enums.TicketStatus;
import com.csci334.EventHub.entity.enums.TicketType;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private String ticketCode;
    private LocalDateTime issuedAt;

    @OneToOne(mappedBy = "ticket")
    @JsonBackReference(value = "registration-ticket")
    private Registration registration;

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    @PrePersist
    public void onIssue() {
        this.issuedAt = LocalDateTime.now();
        this.ticketCode = generateTicketCode();
    }

    private String generateTicketCode() {
        String prefix = (registration != null && registration.getEvent() != null)
                ? registration.getEvent().getTitle().split(" ")[0].toUpperCase()
                : "EVT";
        String randomCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + randomCode;
    }
}

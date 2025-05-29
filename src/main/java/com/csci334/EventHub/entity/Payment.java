package com.csci334.EventHub.entity;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import com.csci334.EventHub.entity.enums.RefundStatus;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Payment {
    @Id
    @Column(length = 6, updatable = false)
    private String id;

    private Double amount;
    private String transactionId;
    private String cardLastFour;
    private LocalDateTime paidAt;
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus = RefundStatus.NONE;

    @Column(columnDefinition = "TEXT")
    private String refundReason;

    private LocalDateTime refundedAt;
    private String status; // e.g., "SUCCESS", "FAILED"

    @OneToOne
    @JoinColumn(name = "registration_id", nullable = false)
    @JsonBackReference(value = "registration-payment")
    private Registration registration;

    private static final SecureRandom RANDOM = new SecureRandom();

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    @PrePersist
    protected void onCreate() {
        // assign a 6‑digit ID if not set
        if (this.id == null) {
            this.id = String.format("%06d", RANDOM.nextInt(1_000_000));
        }
        // optional: set paymentDate if you want a default timestamp
        if (this.paidAt == null) {
            this.paidAt = LocalDateTime.now();
        }
    }
}

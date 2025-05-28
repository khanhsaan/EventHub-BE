package com.csci334.EventHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.csci334.EventHub.entity.Payment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    // All payments for a given registration
    List<Payment> findByRegistrationId(String registrationId);
}

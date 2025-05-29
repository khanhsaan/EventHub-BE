package com.csci334.EventHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.csci334.EventHub.entity.Payment;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByRegistrationId(String registrationId);
}

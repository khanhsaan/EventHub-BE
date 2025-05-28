package com.csci334.EventHub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csci334.EventHub.dto.PaymentMakeDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Payment;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.Ticket;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketStatus;
import com.csci334.EventHub.entity.enums.TicketType;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.PaymentRepository;
import com.csci334.EventHub.repository.RegistrationRepository;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final RegistrationRepository registrationRepo;
    private final EventRepository eventRepo;

    public PaymentService(PaymentRepository paymentRepo, RegistrationRepository registrationRepo,
            EventRepository eventRepo) {
        this.paymentRepo = paymentRepo;
        this.registrationRepo = registrationRepo;
        this.eventRepo = eventRepo;
    }

    @Transactional
    public Payment makePayment(PaymentMakeDTO paymentMakeDTO) {
        String registrationId = paymentMakeDTO.getRegistrationId();
        Registration reg = registrationRepo
                .findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (!reg.getStatus().equals(RegistrationStatus.APPROVED)) {
            throw new RuntimeException("Only approved registrations can be paid for.");
        }

        Payment payment = new Payment();
        payment.setAmount(reg.getAmountDue());
        payment.setTransactionId("TXN-" + System.currentTimeMillis());
        payment.setCardLastFour(paymentMakeDTO.getCardLastFour());
        payment.setStatus("SUCCESS");
        payment.setRegistration(reg);
        payment.setPaidAt(LocalDateTime.now());

        paymentRepo.save(payment);

        // Update registration status
        reg.setStatus(RegistrationStatus.PAID);

        // Generate ticket
        Ticket ticket = new Ticket();
        ticket.setTicketType(reg.getRequestedTicketType());
        ticket.setStatus(TicketStatus.ISSUED);
        ticket.setRegistration(reg);
        reg.setTicket(ticket);

        // Update event capacity
        Event event = reg.getEvent();
        if (reg.getRequestedTicketType().equals(TicketType.VIP)) {
            event.setVipTicketsRemaining(event.getVipTicketsRemaining() - 1);
        } else if (reg.getRequestedTicketType().equals(TicketType.GENERAL)) {
            event.setGeneralTicketsRemaining(event.getGeneralTicketsRemaining() - 1);
        }

        eventRepo.save(event);
        registrationRepo.save(reg);

        return payment;
    }

    @Transactional
    public Payment refundPayment(String paymentId, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus("REFUNDED");
        payment.setRefundedAt(LocalDateTime.now());

        // Optional: update the related registration status
        Registration reg = payment.getRegistration();
        reg.setStatus(com.csci334.EventHub.entity.enums.RegistrationStatus.REFUNDED);

        return paymentRepo.save(payment);
    }
}
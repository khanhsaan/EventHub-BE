package com.csci334.EventHub.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csci334.EventHub.dto.PaymentMakeDTO;
import com.csci334.EventHub.dto.analytics.AnalyticsOverviewDTO;
import com.csci334.EventHub.dto.analytics.SalesEntryDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Payment;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.Ticket;
import com.csci334.EventHub.entity.enums.RefundStatus;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketStatus;
import com.csci334.EventHub.entity.enums.TicketType;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.PaymentRepository;
import com.csci334.EventHub.repository.RegistrationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final RegistrationRepository registrationRepo;
    private final EventRepository eventRepo;
    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;
    private final SimpMessagingTemplate messagingTemplate;

    public PaymentService(PaymentRepository paymentRepo, RegistrationRepository registrationRepo,
            NotificationService notificationService, AnalyticsService analyticsService,
            SimpMessagingTemplate messagingTemplate,
            EventRepository eventRepo) {
        this.paymentRepo = paymentRepo;
        this.registrationRepo = registrationRepo;
        this.eventRepo = eventRepo;
        this.analyticsService = analyticsService;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
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
        reg.setAmountPaid(reg.getAmountDue());

        // Update event capacity
        Event event = reg.getEvent();
        if (reg.getRequestedTicketType().equals(TicketType.VIP)) {
            event.setVipTicketsRemaining(event.getVipTicketsRemaining() - 1);
        } else if (reg.getRequestedTicketType().equals(TicketType.GENERAL)) {
            event.setGeneralTicketsRemaining(event.getGeneralTicketsRemaining() - 1);
        }

        eventRepo.save(event);
        registrationRepo.save(reg);

        SalesEntryDTO dto = new SalesEntryDTO();
        dto.setDate(LocalDate.now().toString());

        if (reg.getRequestedTicketType() == TicketType.VIP) {
            dto.setVip(1);
            dto.setGeneral(0);
        } else {
            dto.setVip(0);
            dto.setGeneral(1);
        }

        String organizerId = reg.getEvent().getOrganizer().getId();
        messagingTemplate.convertAndSend("/topic/sales/" + organizerId, dto);

        AnalyticsOverviewDTO overview = analyticsService.getOverview(reg.getEvent().getOrganizer().getId());
        messagingTemplate.convertAndSend("/topic/analytics/" + organizerId, overview);

        return payment;
    }

    @Transactional
    public Payment refundByOrganizer(String registrationId, String reason) {
        Payment payment = paymentRepo.findByRegistrationId(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus("REFUNDED");
        payment.setRefundStatus(RefundStatus.COMPLETED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundReason(reason);

        Registration reg = payment.getRegistration();
        reg.setStatus(RegistrationStatus.REFUNDED);
        reg.getTicket().setStatus(TicketStatus.REFUNDED);

        TicketType ticketType = reg.getTicket().getTicketType();
        if (ticketType == TicketType.VIP) {
            reg.getEvent().setVipTicketsRemaining(reg.getEvent().getVipTicketsRemaining() + 1);
        } else if (ticketType == TicketType.GENERAL) {
            reg.getEvent().setGeneralTicketsRemaining(reg.getEvent().getGeneralTicketsRemaining() + 1);
        }

        registrationRepo.save(reg);
        notificationService.sendNotification(
                reg.getAttendee().getId(),
                "Refund Processed by Organizer",
                "Your payment for the event \"" + reg.getEvent().getTitle()
                        + "\" has been refunded by the organizer. Reason: " + reason);
        AnalyticsOverviewDTO overview = analyticsService.getOverview(reg.getEvent().getOrganizer().getId());
        messagingTemplate.convertAndSend("/topic/analytics/" + reg.getEvent().getOrganizer().getId(), overview);

        return paymentRepo.save(payment);
    }

    @Transactional
    public Payment requestRefund(String registrationId, String reason) {
        Payment payment = paymentRepo.findByRegistrationId(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        Registration reg = payment.getRegistration();
        if (!"PAID".equals(reg.getStatus().toString())) {
            throw new IllegalStateException("Only paid registrations can request a refund.");
        }

        payment.setRefundStatus(RefundStatus.REQUESTED);
        payment.setRefundReason(reason);

        reg.setStatus(RegistrationStatus.REFUND_REQUESTED);
        registrationRepo.save(reg);
        return paymentRepo.save(payment);
    }

    @Transactional
    public Payment approveRefund(String registrationId) {
        Payment payment = paymentRepo.findByRegistrationId(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new IllegalStateException("Refund is not in REQUESTED state.");
        }

        payment.setRefundStatus(RefundStatus.COMPLETED);
        payment.setStatus("REFUNDED");
        payment.setRefundedAt(LocalDateTime.now());

        Registration reg = payment.getRegistration();
        reg.setStatus(RegistrationStatus.REFUNDED);
        reg.getTicket().setStatus(TicketStatus.REFUNDED);

        TicketType ticketType = reg.getTicket().getTicketType();
        if (ticketType == TicketType.VIP) {
            reg.getEvent().setVipTicketsRemaining(reg.getEvent().getVipTicketsRemaining() + 1);
        } else if (ticketType == TicketType.GENERAL) {
            reg.getEvent().setGeneralTicketsRemaining(reg.getEvent().getGeneralTicketsRemaining() + 1);
        }

        registrationRepo.save(reg);
        notificationService.sendNotification(
                reg.getAttendee().getId(),
                "Refund Approved",
                "Your refund request for the event \"" + reg.getEvent().getTitle()
                        + "\" has been approved. The payment has been refunded.");

        AnalyticsOverviewDTO overview = analyticsService.getOverview(reg.getEvent().getOrganizer().getId());
        messagingTemplate.convertAndSend("/topic/analytics/" + reg.getEvent().getOrganizer().getId(), overview);
        return paymentRepo.save(payment);
    }

    @Transactional
    public Payment rejectRefund(String registrationId) {
        Payment payment = paymentRepo.findByRegistrationId(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new IllegalStateException("Refund is not in REQUESTED state.");
        }

        payment.setRefundStatus(RefundStatus.REJECTED);
        payment.setStatus("SUCCESS");

        Registration reg = payment.getRegistration();
        reg.setStatus(RegistrationStatus.PAID);

        registrationRepo.save(reg);

        notificationService.sendNotification(
                reg.getAttendee().getId(),
                "Refund Rejected",
                "Your refund request for the event \"" + reg.getEvent().getTitle() + "\" was rejected.");
        return paymentRepo.save(payment);
    }

}
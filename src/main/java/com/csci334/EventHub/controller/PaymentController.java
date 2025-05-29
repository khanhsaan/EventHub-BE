package com.csci334.EventHub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.csci334.EventHub.dto.PaymentMakeDTO;
import com.csci334.EventHub.entity.Payment;
import com.csci334.EventHub.service.PaymentService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentSvc;

    public PaymentController(PaymentService paymentSvc) {
        this.paymentSvc = paymentSvc;
    }

    @PostMapping("/make")
    public ResponseEntity<String> makePayment(@RequestBody PaymentMakeDTO request) {
        paymentSvc.makePayment(request);
        return ResponseEntity.ok("Payment successful and ticket generated.");
    }

    @PostMapping("/{registrationId}/refund")
    public ResponseEntity<Payment> refundByOrganizer(@PathVariable String registrationId,
            @RequestBody Map<String, String> payload) {
        String reason = payload.getOrDefault("reason", "Organizer-initiated refund");
        return ResponseEntity.ok(paymentSvc.refundByOrganizer(registrationId, reason));
    }

    @PostMapping("/{registrationId}/request-refund")
    public ResponseEntity<Payment> requestRefund(@PathVariable String registrationId,
            @RequestBody Map<String, String> payload) {
        String reason = payload.getOrDefault("reason", "No reason provided");
        return ResponseEntity.ok(paymentSvc.requestRefund(registrationId, reason));
    }

    @PutMapping("/{registrationId}/approve-refund")
    public ResponseEntity<Payment> approveRefund(@PathVariable String registrationId) {
        Payment approvedRefund = paymentSvc.approveRefund(registrationId);
        return ResponseEntity.ok(approvedRefund);
    }

    @PutMapping("/{registrationId}/reject-refund")
    public ResponseEntity<Payment> rejectRefund(@PathVariable String registrationId) {
        Payment rejectedRefund = paymentSvc.rejectRefund(registrationId);
        return ResponseEntity.ok(rejectedRefund);
    }

}

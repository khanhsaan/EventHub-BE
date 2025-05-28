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

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Payment> refund(@PathVariable String paymentId, @RequestBody Map<String, String> payload) {
        String reason = payload.getOrDefault("reason", "No reason provided");
        Payment refunded = paymentSvc.refundPayment(paymentId, reason);
        return ResponseEntity.ok(refunded);
    }
}

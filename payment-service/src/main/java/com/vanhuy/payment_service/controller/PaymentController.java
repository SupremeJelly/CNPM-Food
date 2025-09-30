package com.vanhuy.payment_service.controller;


import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vanhuy.payment_service.dto.PaymentRequest;
import com.vanhuy.payment_service.model.Payment;
import com.vanhuy.payment_service.service.PaymentService;


@RestController
@RequestMapping("/payments")
public class PaymentController {


    @Autowired
    private PaymentService service;


    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody PaymentRequest req) {
        Payment p = new Payment();
        p.setOrderId(req.getOrderId());
        // p.setAmount(req.getAmount());
        p.setMethod(req.getMethod());
        Payment saved = service.createPayment(p);
        return ResponseEntity.created(URI.create("/payments/" + saved.getId())).body(saved);
    }


    @GetMapping
    public List<Payment> all() {
        return service.getAll();
    }


    @GetMapping("/{id}")
        public ResponseEntity<Payment> get(@PathVariable Long id) {
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/order/{orderId}")
        public List<Payment> byOrder(@PathVariable Long orderId) {
        return service.getByOrderId(orderId);
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Payment updated = service.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
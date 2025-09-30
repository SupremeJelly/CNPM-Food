package com.vanhuy.payment_service.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vanhuy.payment_service.model.Payment;
import com.vanhuy.payment_service.repository.PaymentRepository;


@Service
public class PaymentService {


    @Autowired
    private PaymentRepository repo;


    public Payment createPayment(Payment p) {
        p.setStatus("PENDING");
        p.setCreatedAt(LocalDateTime.now());
        return repo.save(p);
    }


    public Optional<Payment> getById(Long id) {
        return repo.findById(id);
    }


    public List<Payment> getAll() {
        return repo.findAll();
    }


    public List<Payment> getByOrderId(Long orderId) {
        return repo.findByOrderId(orderId);
    }


    public Payment updateStatus(Long id, String status) {
        Payment p = repo.findById(id).orElseThrow();
        p.setStatus(status);
        return repo.save(p);
    }
}
package com.vanhuy.payment_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vanhuy.payment_service.client.OrderClient;
import com.vanhuy.payment_service.model.Payment;
import com.vanhuy.payment_service.model.Payment.PaymentStatus;
import com.vanhuy.payment_service.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repo;

    @Autowired
    private OrderClient orderClient;

    public Payment createPayment(Payment p) {
        p.setStatus(PaymentStatus.PENDING);
        p.setCreatedAt(LocalDateTime.now());
        Payment saved = repo.save(p);

        // Sau khi lưu có thể thông báo sang Order service (tùy logic)
        orderClient.updatePaymentStatus(p.getOrderId(), p.getStatus().name());
        return saved;
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

    public Payment updateStatus(Long id, PaymentStatus status) {
        Payment p = repo.findById(id).orElseThrow();
        p.setStatus(status);
        Payment updated = repo.save(p);

        // Cập nhật sang Order Service
        orderClient.updatePaymentStatus(p.getOrderId(), status.name());
        return updated;
    }
}























// package com.vanhuy.payment_service.service;


// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.vanhuy.payment_service.model.Payment;
// import com.vanhuy.payment_service.model.Payment.PaymentStatus;
// import com.vanhuy.payment_service.repository.PaymentRepository;


// @Service
// public class PaymentService {


//     @Autowired
//     private PaymentRepository repo;


//     public Payment createPayment(Payment p) {
//         p.setStatus(PaymentStatus.PENDING);
//         p.setCreatedAt(LocalDateTime.now());
//         return repo.save(p);
//     }


//     public Optional<Payment> getById(Long id) {
//         return repo.findById(id);
//     }


//     public List<Payment> getAll() {
//         return repo.findAll();
//     }


//     public List<Payment> getByOrderId(Long orderId) {
//         return repo.findByOrderId(orderId);
//     }


//     public Payment updateStatus(Long id, PaymentStatus status) {
//         Payment p = repo.findById(id).orElseThrow();
//         p.setStatus(status);
//         orderClient.updatePaymentStatus(p.getOrderId(), status.name());
//         return repo.save(p);
//     }

// }

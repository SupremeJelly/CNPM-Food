package com.vanhuy.payment_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vanhuy.payment_service.client.OrderClient;
import com.vanhuy.payment_service.client.RestaurantClient;
import com.vanhuy.payment_service.model.Payment;
import com.vanhuy.payment_service.model.Payment.PaymentStatus;
import com.vanhuy.payment_service.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repo;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private RestaurantClient restaurantClient;

    public Payment createPayment(Payment p) {
        p.setStatus(PaymentStatus.PAID);
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


    public Payment processPayment(Integer orderId, String method) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.valueOf(method.toUpperCase());
        payment.setMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PAID);
        payment.setCreatedAt(LocalDateTime.now());

        Payment saved = repo.save(payment);

            try {
                // Cập nhật trạng thái thanh toán
                orderClient.updatePaymentStatus(orderId, PaymentStatus.PAID.name());

                // --- Giảm stock tự động ---
                List<OrderClient.OrderItemDTO> orderItems = orderClient.getOrderItems(orderId);

                // Kiểm tra xem orderItems có dữ liệu không
                System.out.println("Order items received: " + orderItems);

                // Chuyển sang danh sách stock
                List<RestaurantClient.StockDecrementRequest.Item> items = orderItems.stream()
                    .map(i -> new RestaurantClient.StockDecrementRequest.Item(i.getMenuItemId(), i.getQuantity()))
                    .collect(Collectors.toList());

                restaurantClient.decreaseStock(new RestaurantClient.StockDecrementRequest(items));
                System.out.println("Stock decreased successfully!");
                // --------------------------------
            } catch (Exception e) {
                System.err.println("Error while processing stock/payment update:");
                e.printStackTrace(); // in chi tiết lỗi ra console
            }

        return saved;
    }



}

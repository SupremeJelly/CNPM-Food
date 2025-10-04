package com.vanhuy.payment_service.model;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // Dùng enum thay cho String

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // Enum cho trạng thái thanh toán

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentMethod {
        CREDIT_CARD,
        BANK_TRANSFER,
        CASH_ON_DELIVERY,
        PAYPAL,
        MOMO,
        ZALOPAY
    }

    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED
    }


    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;


    // private Long orderId;
    // private Double amount;
    // private String method; // e.g., CREDIT_CARD, CASH
    // private String status; // PENDING, SUCCESS, FAILED


    // private LocalDateTime createdAt;


    // public Payment() {}


    // // getters & setters
    // public Long getId() { return id; }
    // public void setId(Long id) { this.id = id; }
    // public Long getOrderId() { return orderId; }
    // public void setOrderId(Long orderId) { this.orderId = orderId; }
    // public Double getAmount() { return amount; }
    // public void setAmount(Double amount) { this.amount = amount; }
    // public String getMethod() { return method; }
    // public void setMethod(String method) { this.method = method; }
    // public String getStatus() { return status; }
    // public void setStatus(String status) { this.status = status; }
    // public LocalDateTime getCreatedAt() { return createdAt; }
    // public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
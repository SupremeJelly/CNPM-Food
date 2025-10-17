// package com.vanhuy.payment_service.dto;


// public class PaymentRequest {
// private Long orderId;
// private Double amount;
// private String method;


// public Long getOrderId() { return orderId; }
// public void setOrderId(Long orderId) { this.orderId = orderId; }
// public Double getAmount() { return amount; }
// public void setAmount(Double amount) { this.amount = amount; }
// public String getMethod() { return method; }
// public void setMethod(String method) { this.method = method; }
// }



package com.vanhuy.payment_service.dto;

import java.math.BigDecimal;

import com.vanhuy.payment_service.model.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private Integer orderId;
    private Long userId;           // thêm userId (tùy nhu cầu)
    private BigDecimal amount;     // thay Double bằng BigDecimal
    private Payment.PaymentMethod method;
    

    public Integer getOrderId() {
        return orderId;
    }
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, BANK_TRANSFER, CASH_ON_DELIVERY
    }

    // public enum PaymentStatus {
    //     SUCCESS, FAILED, PENDING
    // }
}
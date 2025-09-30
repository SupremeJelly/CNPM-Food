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

public class PaymentRequest {

    private Long orderId;
    private Long userId;           // thêm userId (tùy nhu cầu)
    private BigDecimal amount;     // thay Double bằng BigDecimal
    private String method;         // hoặc tạo enum PaymentMethod

    public Long getOrderId() {
        return orderId;
    }
    public void setOrderId(Long orderId) {
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

    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
}
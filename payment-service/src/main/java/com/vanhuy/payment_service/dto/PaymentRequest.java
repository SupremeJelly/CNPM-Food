package com.vanhuy.payment_service.dto;


public class PaymentRequest {
private Long orderId;
private Double amount;
private String method;


public Long getOrderId() { return orderId; }
public void setOrderId(Long orderId) { this.orderId = orderId; }
public Double getAmount() { return amount; }
public void setAmount(Double amount) { this.amount = amount; }
public String getMethod() { return method; }
public void setMethod(String method) { this.method = method; }
}
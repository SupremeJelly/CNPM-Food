package com.vanhuy.payment_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về cho frontend sau khi xử lý thanh toán.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long paymentId;           // ID giao dịch thanh toán
    private Long orderId;             // ID đơn hàng liên quan
    private BigDecimal amount;        // Số tiền đã thanh toán
    private String method;            // Phương thức thanh toán
    private String status;            // "SUCCESS" | "FAILED" | "PENDING"
    private String transactionCode;   // Mã giao dịch (nếu có)
    private LocalDateTime timestamp;  // Thời điểm thanh toán
    private String message;           // Thông báo kết quả
}

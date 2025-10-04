package com.vanhuy.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Gọi sang service order-service
@FeignClient(name = "order-service", url = "http://localhost:8082/api/v1")
public interface OrderClient {

    // API để cập nhật trạng thái thanh toán
    @PutMapping("/orders/{orderId}/payment-status")
    void updatePaymentStatus(
        @PathVariable("orderId") Long orderId,
        @RequestParam("status") String status
    );
}
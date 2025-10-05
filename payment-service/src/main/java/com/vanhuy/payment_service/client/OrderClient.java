package com.vanhuy.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// // Gọi sang service order-service
// @FeignClient(name = "order-service", url = "http://localhost:8083/api/v1")
// public interface OrderClient {

//     // API để cập nhật trạng thái thanh toán
//     @PutMapping("/orders/{orderId}/payment-status")
//     void updatePaymentStatus(
//         @PathVariable("orderId") Integer orderId,
//         @RequestParam("status") String status
//     );
// }

@FeignClient(name = "orderClient", url = "http://localhost:8083")
public interface OrderClient {

    @PutMapping("/api/v1/orders/{orderId}/payment-status")
    void updatePaymentStatus(
        @PathVariable("orderId") Integer orderId,
        @RequestParam("status") String status
    );
}
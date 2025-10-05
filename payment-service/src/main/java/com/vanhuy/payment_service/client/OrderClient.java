package com.vanhuy.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    
    @GetMapping("/api/v1/orders/{orderId}/items")
    List<OrderItemDTO> getOrderItems(@PathVariable("orderId") Integer orderId);

    class OrderItemDTO {
        private Integer menuItemId;
        private Integer quantity;

        public OrderItemDTO() {}

        public OrderItemDTO(Integer menuItemId, Integer quantity) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
        }

        public Integer getMenuItemId() { return menuItemId; }
        public void setMenuItemId(Integer menuItemId) { this.menuItemId = menuItemId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
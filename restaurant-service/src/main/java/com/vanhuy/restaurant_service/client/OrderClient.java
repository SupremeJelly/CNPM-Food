package com.vanhuy.restaurant_service.client;

import com.vanhuy.order_service.dto.OrderItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service", url = "http://localhost:8083/api/v1")
public interface OrderClient {

    @GetMapping("/orders/{orderId}/items")
    List<OrderItemDTO> getOrderItems(@PathVariable("orderId") Integer orderId);
}

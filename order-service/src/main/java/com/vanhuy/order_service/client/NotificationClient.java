package com.vanhuy.order_service.client;

import com.vanhuy.order_service.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${NOTIFICATION_SERVICE_URL:http://localhost:8084/api/v1/notifications}")
public interface NotificationClient {
    @PostMapping("/order")
    String sendOrderNotification(@RequestBody OrderResponse orderResponse);
}

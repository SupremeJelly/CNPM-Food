package com.yourcompany.order.client;

import com.vanhuy.order_service.dto.PaymentRequest;
import com.vanhuy.order_service.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", url = "http://localhost:8084")
public interface PaymentClient {

    @PostMapping("/api/payments/charge")
    PaymentResponse charge(@RequestBody PaymentRequest request);

    @GetMapping("/api/payments/{id}")
    PaymentResponse getPayment(@PathVariable("id") String id);
}

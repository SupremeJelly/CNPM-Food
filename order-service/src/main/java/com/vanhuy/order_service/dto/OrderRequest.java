package com.vanhuy.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private Integer userId;
    private String recipientName;
    private String contactEmail;
    private String shippingAddress;
    private String contactPhone;
    private List<OrderItemRequest> items;
}

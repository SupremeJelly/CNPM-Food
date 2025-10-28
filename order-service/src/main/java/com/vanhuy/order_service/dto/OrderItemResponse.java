package com.vanhuy.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
// DTO đại diện cho một mục trong đơn hàng

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Integer orderItemId;
    private Integer menuItemId;
    private Integer quantity;
    private BigDecimal subtotal;
}

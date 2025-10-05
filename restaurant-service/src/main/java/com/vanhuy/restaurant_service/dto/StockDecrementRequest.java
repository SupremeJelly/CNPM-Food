package com.vanhuy.restaurant_service.dto;

import java.util.List;

public record StockDecrementRequest(
        List<Item> items
) {
    public record Item(
        Integer menuItemId,
        Integer quantity
    ) {}
}

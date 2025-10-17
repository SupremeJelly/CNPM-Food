package com.vanhuy.payment_service.client;  // Nếu dùng trong payment-service, đổi package tương ứng

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// Feign Client gọi sang restaurant-service
@FeignClient(name = "restaurant-service", url = "http://localhost:8082/api/v1/stock")
public interface RestaurantClient {

    @PutMapping("/decrement")
    void decreaseStock(@RequestBody StockDecrementRequest request);

    class StockDecrementRequest {
        private List<Item> items;
        public StockDecrementRequest() {}
        public StockDecrementRequest(List<Item> items) { this.items = items; }
        public List<Item> getItems() { return items; }
        public void setItems(List<Item> items) { this.items = items; }
        public static class Item {
            private Integer menuItemId;
            private Integer quantity;
            public Item() {}
            public Item(Integer menuItemId, Integer quantity) {
                this.menuItemId = menuItemId;
                this.quantity = quantity;
            }
            public Integer getMenuItemId() { return menuItemId; }
            public void setMenuItemId(Integer menuItemId) { this.menuItemId = menuItemId; }
            public Integer getQuantity() { return quantity; }
            public void setQuantity(Integer quantity) { this.quantity = quantity; }
        }
    }
}

package com.vanhuy.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "restaurant-service", url = "${RESTAURANT_SERVICE_URL:http://localhost:8082}")
public interface RestaurantClient {
    @GetMapping("/api/v1/menu-items/{menuItemId}")
    BigDecimal getPriceByMenuItemId(@PathVariable Integer menuItemId);
    
    @PutMapping("/api/v1/stock/decrement")
    void decreaseStock(@RequestBody StockDecrementRequest request);
    
    // DTO for stock decrement
    class StockDecrementRequest {
        private List<Item> items;
        
        public StockDecrementRequest() {}
        
        public StockDecrementRequest(List<Item> items) {
            this.items = items;
        }
        
        public List<Item> getItems() {
            return items;
        }
        
        public void setItems(List<Item> items) {
            this.items = items;
        }
        
        public static class Item {
            private Integer menuItemId;
            private Integer quantity;
            
            public Item() {}
            
            public Item(Integer menuItemId, Integer quantity) {
                this.menuItemId = menuItemId;
                this.quantity = quantity;
            }
            
            public Integer getMenuItemId() {
                return menuItemId;
            }
            
            public void setMenuItemId(Integer menuItemId) {
                this.menuItemId = menuItemId;
            }
            
            public Integer getQuantity() {
                return quantity;
            }
            
            public void setQuantity(Integer quantity) {
                this.quantity = quantity;
            }
        }
    }
}

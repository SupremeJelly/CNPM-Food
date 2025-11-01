package com.vanhuy.restaurant_service.controller;

import com.vanhuy.restaurant_service.dto.OrderItemDTO;
import com.vanhuy.restaurant_service.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stock")
public class StockController {
    private final StockService stockService;

    @PutMapping("/decrement")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decrement(@RequestBody StockDecrementRequest request) {
        stockService.decreaseStock(request.getItems());
    }

    @PostMapping("/decrease")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decrease(@RequestBody List<OrderItemDTO> items) {
        stockService.decreaseStock(items);
    }

    @PostMapping("/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void increase(@RequestBody List<OrderItemDTO> items) {
        stockService.increaseStock(items);
    }
    
    // DTO class for decrement request
    public static class StockDecrementRequest {
        private List<OrderItemDTO> items;
        
        public StockDecrementRequest() {}
        
        public StockDecrementRequest(List<OrderItemDTO> items) {
            this.items = items;
        }
        
        public List<OrderItemDTO> getItems() {
            return items;
        }
        
        public void setItems(List<OrderItemDTO> items) {
            this.items = items;
        }
    }
}

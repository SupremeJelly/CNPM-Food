package com.vanhuy.restaurant_service.controller;

import com.vanhuy.restaurant_service.dto.StockDecrementRequest;
import com.vanhuy.restaurant_service.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PutMapping("/decrement")
    public ResponseEntity<Void> decreaseStock(@RequestBody StockDecrementRequest request) {
        stockService.decreaseStock(request);
        return ResponseEntity.ok().build();
    }
}

package com.vanhuy.restaurant_service.service;

import com.vanhuy.restaurant_service.dto.OrderItemDTO;
import com.vanhuy.restaurant_service.exception.OutOfStockException;
import com.vanhuy.restaurant_service.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public void decreaseStock(List<OrderItemDTO> items) {
        for (OrderItemDTO it : items) {
            int updated = menuItemRepository.decreaseStock(it.getMenuItemId(), it.getQuantity());
            if (updated == 0) {
                throw new OutOfStockException("Out of stock for itemId=" + it.getMenuItemId());
            }
        }
    }

    @Transactional
    public void increaseStock(List<OrderItemDTO> items) {
        for (OrderItemDTO it : items) {
            menuItemRepository.increaseStock(it.getMenuItemId(), it.getQuantity());
        }
    }
}

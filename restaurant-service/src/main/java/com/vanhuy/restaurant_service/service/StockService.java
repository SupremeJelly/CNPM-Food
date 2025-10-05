package com.vanhuy.restaurant_service.service;

import com.vanhuy.restaurant_service.dto.StockDecrementRequest;
import com.vanhuy.restaurant_service.exception.OutOfStockException;
import com.vanhuy.restaurant_service.model.MenuItem;
import com.vanhuy.restaurant_service.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final MenuItemRepository menuItemRepository;

    public StockService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public void decreaseStock(StockDecrementRequest request) {
        List<Integer> ids = request.items().stream()
                .map(StockDecrementRequest.Item::menuItemId)
                .toList();

        Map<Integer, MenuItem> menuItems = menuItemRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(MenuItem::getMenuItemId, m -> m));

        // Kiểm tra stock đủ cho tất cả món
        for (StockDecrementRequest.Item item : request.items()) {
            MenuItem menuItem = menuItems.get(item.menuItemId());
            if (menuItem == null) {
                throw new RuntimeException("Menu item not found: " + item.menuItemId());
            }
            if (menuItem.getStock() < item.quantity()) {
                throw new OutOfStockException("Not enough stock for menu item: " + menuItem.getName());
            }
        }

        // Giảm stock
        for (StockDecrementRequest.Item item : request.items()) {
            MenuItem menuItem = menuItems.get(item.menuItemId());
            menuItem.setStock(menuItem.getStock() - item.quantity());
        }

        menuItemRepository.saveAll(menuItems.values());
    }
}

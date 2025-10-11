package com.vanhuy.restaurant_service.dto;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Integer menuItemId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Integer quantity;

    public OrderItemDTO() {}

    // constructor 5 tham số (cho toDTO trong MenuItemService)
    public OrderItemDTO(Integer menuItemId, String name, BigDecimal price, Integer stock, String imageUrl) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.quantity = null;  // hoặc 0
    }

    // constructor 6 tham số (cho StockService)
    public OrderItemDTO(Integer menuItemId, String name, BigDecimal price, Integer stock, String imageUrl, Integer quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
    }

    // getters and setters 
    public Integer getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Integer menuItemId) { this.menuItemId = menuItemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
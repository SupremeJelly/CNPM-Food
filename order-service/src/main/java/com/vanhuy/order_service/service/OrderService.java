package com.vanhuy.order_service.service;

import com.vanhuy.order_service.client.NotificationClient;
import com.vanhuy.order_service.client.RestaurantClient;
import com.vanhuy.order_service.client.UserServiceClient;
import com.vanhuy.order_service.constant.Constants;
import com.vanhuy.order_service.dto.OrderItemResponse;
import com.vanhuy.order_service.dto.OrderRequest;
import com.vanhuy.order_service.dto.OrderResponse;
import com.vanhuy.order_service.dto.UserDTO;
import com.vanhuy.order_service.dto.OrderItemDTO; 
import com.vanhuy.order_service.exception.ResourceNotFoundException;
import com.vanhuy.order_service.model.Order;
import com.vanhuy.order_service.model.OrderItem;
import com.vanhuy.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import feign.FeignException;

@Service
@RequiredArgsConstructor
public class OrderService {

    Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final UserServiceClient userServiceClient;
    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;
    private final NotificationClient notificationClient;

    // create order
    public OrderResponse createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setRecipientName(orderRequest.getRecipientName());
        order.setContactEmail(orderRequest.getContactEmail());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setContactPhone(orderRequest.getContactPhone());

        List<OrderItem> orderItems = orderRequest.getItems().stream()
                .map(orderItemRequest -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setMenuItemId(orderItemRequest.getMenuItemId());
                    orderItem.setQuantity(orderItemRequest.getQuantity());
                    orderItem.setSubtotal(
                            calculateSubtotal(orderItemRequest.getMenuItemId(), orderItemRequest.getQuantity()));
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // calculate tax
        BigDecimal tax = subtotal.multiply(Constants.TAX_RATE);

        order.setTotalAmount(subtotal.add(tax));

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created: " + savedOrder);

        // Decrease stock after order is created
        try {
            List<RestaurantClient.StockDecrementRequest.Item> stockItems = orderItems.stream()
                    .map(item -> new RestaurantClient.StockDecrementRequest.Item(
                            item.getMenuItemId(), 
                            item.getQuantity()
                    ))
                    .collect(Collectors.toList());
            
            restaurantClient.decreaseStock(new RestaurantClient.StockDecrementRequest(stockItems));
            logger.info("Stock decreased successfully for order: " + savedOrder.getOrderId());
        } catch (Exception e) {
            logger.error("Failed to decrease stock for order: " + savedOrder.getOrderId(), e);
            // You may want to handle this - either rollback order or mark it for manual review
            // For now, we'll log and continue
        }

        // send order notification
        OrderResponse orderResponse = orderToOrderResponse(savedOrder);
        sendOrderNotification(orderResponse);

        return orderResponse;
    }

    // private BigDecimal calculateSubtotal(Integer menuItemId, Integer quantity) {
    //     BigDecimal price = restaurantClient.getPriceByMenuItemId(menuItemId);
    //     return price.multiply(BigDecimal.valueOf(quantity));
    //     //  try {
    //     //     BigDecimal price = restaurantClient.getPriceByMenuItemId(menuItemId);
    //     //     return price.multiply(BigDecimal.valueOf(quantity));
    //     // } catch (Exception e) {
    //     //     logger.error("Failed to fetch price for menuItemId {}: {}", menuItemId, e.getMessage());
    //     //     // fallback tạm thời
    //     //     return BigDecimal.ZERO;
    //     // }
    // }

    // Trong file OrderService.java
    private BigDecimal calculateSubtotal(Integer menuItemId, Integer quantity) {
        BigDecimal price; // 1. Khai báo price ở ngoài

        try {
            // 2. Khối try-catch này CHỈ DÙNG để bắt lỗi từ client (như 404, 500)
            price = restaurantClient.getPriceByMenuItemId(menuItemId);

        } catch (FeignException.NotFound e) {
            logger.error("Menu item not found via restaurantClient: {}", menuItemId, e);
            throw new ResourceNotFoundException("Menu item not found: " + menuItemId); // Ném lỗi 404

        } catch (Exception e) {
            // Bắt tất cả các lỗi khác từ Feign (như 500, timeout)
            logger.error("Failed to fetch price for menuItemId {}: {}", menuItemId, e.getMessage());
            throw new RuntimeException("Error fetching price for menu item: " + menuItemId, e);
        }

        // 3. Logic nghiệp vụ: Xử lý sau khi đã gọi client thành công
        if (price == null) {
            logger.warn("Price for menuItemId {} is null. Assuming item not found.", menuItemId);
            throw new ResourceNotFoundException("Menu item not found or price is missing: " + menuItemId);
        }

        // 4. Nếu price không null, tính toán
        return price.multiply(BigDecimal.valueOf(quantity));
    }


    // get all orders
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::orderToOrderResponse);
    }

    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderToOrderResponse(order);
    }


    private OrderResponse orderToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUserId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setPaymentStatus(order.getPaymentStatus().name());
        response.setRecipientName(order.getRecipientName());
        response.setContactEmail(order.getContactEmail());
        response.setShippingAddress(order.getShippingAddress());
        response.setContactPhone(order.getContactPhone());

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemResponse itemResponse = new OrderItemResponse();
                    itemResponse.setOrderItemId(item.getOrderItemId());
                    itemResponse.setMenuItemId(item.getMenuItemId());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setSubtotal(item.getSubtotal());
                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }

    private void sendOrderNotification(OrderResponse orderResponse) {
        CompletableFuture.runAsync(() -> {
            notificationClient.sendOrderNotification(orderResponse);
        }).exceptionally(ex -> {
            logger.error("Failed to send order notification for order: {}", orderResponse.getOrderId(), ex);
            return null;
        });
    }

    public UserDTO getOrderInfo(Integer userId) {
        UserDTO userDTO = userServiceClient.getUserById(userId);
        return  userDTO;
    }
    @Transactional
    public void updatePaymentStatus(Integer orderId, String status) {
        logger.info("[OrderService] Updating payment status for order {} to '{}'", orderId, status);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> {
                logger.error("[OrderService] Order not found: {}", orderId);
                return new RuntimeException("Order not found: " + orderId);
            });

        try {
            order.setPaymentStatus(Order.PaymentStatus.valueOf(status.toUpperCase()));
            orderRepository.save(order);
            logger.info("[OrderService] Payment status updated successfully: {}", order.getPaymentStatus());
        } catch (IllegalArgumentException ex) {
            logger.error("[OrderService] Invalid payment status received: '{}'", status);
            throw new RuntimeException("Invalid payment status: " + status, ex);
        }
    }

    public List<OrderItemDTO> getOrderItems(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return order.getOrderItems().stream()
                .map(item -> new OrderItemDTO(item.getMenuItemId(), item.getQuantity()))
                .collect(Collectors.toList());
    }
    
}

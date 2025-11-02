package com.vanhuy.order_service.unit.service;

import com.vanhuy.order_service.service.OrderService;
import com.vanhuy.order_service.client.NotificationClient;
import com.vanhuy.order_service.client.RestaurantClient;
import com.vanhuy.order_service.client.UserServiceClient;
import com.vanhuy.order_service.dto.OrderRequest;
import com.vanhuy.order_service.dto.OrderResponse;
import com.vanhuy.order_service.dto.OrderItemRequest;
import com.vanhuy.order_service.exception.ResourceNotFoundException;
import com.vanhuy.order_service.model.Order;
import com.vanhuy.order_service.model.OrderItem;
import com.vanhuy.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private UserServiceClient userServiceClient;
    @Mock private OrderRepository orderRepository;
    @Mock private RestaurantClient restaurantClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private OrderService orderService;

    private Order order;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setUserId(1);
        orderRequest.setRecipientName("Huy");
        orderRequest.setContactEmail("huy@gmail.com");
        orderRequest.setShippingAddress("123 Main St");
        orderRequest.setContactPhone("0123456789");

        OrderItemRequest item = new OrderItemRequest();
        item.setMenuItemId(101);
        item.setQuantity(2);

        orderRequest.setItems(List.of(item));

        order = new Order();
        order.setOrderId(1);
        order.setUserId(1);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(110)); // giả định
    }

    // 🔹 TEST CASE 1: Tạo đơn hàng thành công
    @Test
    void createOrder_ShouldReturnOrderResponse_WhenValidRequest() {
        // Giả lập giá từ RestaurantClient
        when(restaurantClient.getPriceByMenuItemId(101))
            .thenReturn(BigDecimal.valueOf(50));

        // Giả lập repository save
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Gọi hàm
        OrderResponse response = orderService.createOrder(orderRequest);

        // Kiểm tra kết quả
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalAmount()).isGreaterThan(BigDecimal.ZERO);

        // Xác nhận các tương tác đã diễn ra
        verify(restaurantClient).getPriceByMenuItemId(101);
        verify(orderRepository).save(any(Order.class));
        verify(notificationClient).sendOrderNotification(any(OrderResponse.class));
    }

    // 🔹 TEST CASE 2: Khi restaurant-service trả về null giá
    @Test
    void createOrder_ShouldThrowException_WhenPriceIsNull() {
        when(restaurantClient.getPriceByMenuItemId(101)).thenReturn(null);

        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Menu item not found or price is missing");
    }

    // 🔹 TEST CASE 3: Lấy order theo ID thành công
    @Test
    void getOrderById_ShouldReturnOrderResponse_WhenExists() {
        order.setOrderItems(List.of(new OrderItem(1, order, 101, 2, BigDecimal.valueOf(100))));
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1);
        assertThat(response.getItems()).hasSize(1);
    }

    // 🔹 TEST CASE 4: Lấy order theo ID nhưng không tồn tại
    @Test
    void getOrderById_ShouldThrowException_WhenNotFound() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Order not found");
    }

    // 🔹 TEST CASE 5: Cập nhật trạng thái thanh toán thành công
    @Test
    void updatePaymentStatus_ShouldUpdateStatus_WhenValid() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        orderService.updatePaymentStatus(1, "PAID");

        verify(orderRepository).save(order);
        assertThat(order.getPaymentStatus()).isEqualTo(Order.PaymentStatus.PAID);
    }

    // 🔹 TEST CASE 6: Cập nhật trạng thái thanh toán với status không hợp lệ
    @Test
    void updatePaymentStatus_ShouldThrowException_WhenInvalidStatus() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updatePaymentStatus(1, "UNKNOWN"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid payment status");
    }

    // 🔹 TEST CASE 7: Lấy danh sách item của order
    @Test
    void getOrderItems_ShouldReturnItems_WhenOrderExists() {
        OrderItem item = new OrderItem(1, order, 101, 2, BigDecimal.valueOf(100));
        order.setOrderItems(List.of(item));
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        var items = orderService.getOrderItems(1);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getMenuItemId()).isEqualTo(101);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
    }
}

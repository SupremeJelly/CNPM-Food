package com.vanhuy.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.order_service.dto.OrderItemDTO;
import com.vanhuy.order_service.dto.OrderRequest;
import com.vanhuy.order_service.dto.OrderResponse;
import com.vanhuy.order_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Chỉ load Controller layer, không cần Service thật
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Mock layer service để không cần DB
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderResponse = new OrderResponse();
        orderResponse.setOrderId(1);
        orderResponse.setTotalAmount(new BigDecimal("100.00"));
    }

    // ✅ Test POST /api/v1/orders
    @Test
    void testCreateOrder_ShouldReturnCreatedOrder() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(5);
        Mockito.when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    // ✅ Test GET /api/v1/orders/{id}
    @Test
    void testGetOrderById_ShouldReturnOrder() throws Exception {
        Mockito.when(orderService.getOrderById(1)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    // ✅ Test GET /api/v1/orders?page=0&size=10
    @Test
    void testGetAllOrders_ShouldReturnPage() throws Exception {
        Page<OrderResponse> mockPage = new PageImpl<>(List.of(orderResponse));
        Mockito.when(orderService.getAllOrders(any(PageRequest.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/orders?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(1));
    }

    // ✅ Test PUT /api/v1/orders/{id}/payment-status
    @Test
    void testUpdatePaymentStatus_ShouldReturnSuccessMessage() throws Exception {
        Mockito.doNothing().when(orderService).updatePaymentStatus(eq(1), eq("PAID"));

        mockMvc.perform(put("/api/v1/orders/1/payment-status?status=PAID"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment status updated successfully!"));
    }

    // ✅ Test GET /api/v1/orders/{id}/items
    @Test
    void testGetOrderItems_ShouldReturnList() throws Exception {
        OrderItemDTO item1 = new OrderItemDTO(101, 2);
        Mockito.when(orderService.getOrderItems(1)).thenReturn(List.of(item1));

        mockMvc.perform(get("/api/v1/orders/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].menuItemId").value(101))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }
}

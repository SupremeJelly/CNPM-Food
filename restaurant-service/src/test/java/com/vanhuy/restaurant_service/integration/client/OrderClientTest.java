package com.vanhuy.restaurant_service.integration.client;

import com.vanhuy.restaurant_service.client.OrderClient;
import com.vanhuy.restaurant_service.dto.OrderItemDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class OrderClientTest {

    @MockBean
    private OrderClient orderClient;

    @Test
    void testGetOrderItems() {
        List<OrderItemDTO> mockItems = List.of(
            new OrderItemDTO(1, "Pizza", new BigDecimal("10.00"), 20, "pizza.jpg", 2),
            new OrderItemDTO(2, "Burger", new BigDecimal("8.50"), 15, "burger.jpg", 3)
        );

        when(orderClient.getOrderItems(eq(100))).thenReturn(mockItems);

        List<OrderItemDTO> result = orderClient.getOrderItems(100);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMenuItemId()).isEqualTo(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);

        verify(orderClient, times(1)).getOrderItems(100);
    }

    // @Test
    // void testUpdatePaymentStatus() {
    //     Mockito.doNothing().when(orderClient).updatePaymentStatus(123, "PAID");

    //     orderClient.updatePaymentStatus(123, "PAID");

    //     verify(orderClient, times(1)).updatePaymentStatus(123, "PAID");
    // }
}

package com.vanhuy.order_service.integration.client;

import com.vanhuy.order_service.client.RestaurantClient;
import com.vanhuy.order_service.client.RestaurantClient.StockDecrementRequest;
import com.vanhuy.order_service.client.RestaurantClient.StockDecrementRequest.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RestaurantClientTest {

    @MockBean
    private RestaurantClient restaurantClient;

    @Test
    @DisplayName("Mock gọi getPriceByMenuItemId() trả về đúng giá")
    void testGetPriceByMenuItemId() {
        when(restaurantClient.getPriceByMenuItemId(eq(10)))
            .thenReturn(new BigDecimal("45000"));

        BigDecimal price = restaurantClient.getPriceByMenuItemId(10);

        assertThat(price).isNotNull();
        assertThat(price).isEqualByComparingTo("45000");

        verify(restaurantClient, times(1)).getPriceByMenuItemId(10);
    }

    @Test
    @DisplayName("Mock gọi decreaseStock() không lỗi")
    void testDecreaseStock() {
        List<Item> items = List.of(
            new Item(1, 2),
            new Item(2, 1)
        );
        StockDecrementRequest request = new StockDecrementRequest(items);

        Mockito.doNothing().when(restaurantClient).decreaseStock(any(StockDecrementRequest.class));

        restaurantClient.decreaseStock(request);

        verify(restaurantClient, times(1)).decreaseStock(eq(request));
    }
}

package com.vanhuy.payment_service.client;

import com.vanhuy.payment_service.client.RestaurantClient.StockDecrementRequest;
import com.vanhuy.payment_service.client.RestaurantClient.StockDecrementRequest.Item;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class RestaurantClientTest {

    @MockBean
    private RestaurantClient restaurantClient;

    @Test
    void testDecreaseStock() {
        StockDecrementRequest request = new StockDecrementRequest(
            List.of(new Item(10, 3), new Item(11, 1))
        );

        Mockito.doNothing().when(restaurantClient).decreaseStock(any(StockDecrementRequest.class));

        restaurantClient.decreaseStock(request);

        verify(restaurantClient, times(1)).decreaseStock(request);
    }
}

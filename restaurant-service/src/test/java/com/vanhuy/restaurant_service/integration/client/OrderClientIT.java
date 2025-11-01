package com.vanhuy.restaurant_service.integration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.restaurant_service.client.OrderClient;
import com.vanhuy.restaurant_service.dto.OrderItemDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for OrderClient using WireMock to mock order-service responses
 * Tests the Feign client communication without requiring actual order-service to be running
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderClientIT {

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ORDER_SERVICE_BASE_PATH = "/api/v1/orders";

    @BeforeEach
    void setUp() {
        // Reset WireMock before each test
        resetAllRequests();
    }

    @Test
    @Order(1)
    @DisplayName("Should successfully retrieve order items for valid order ID")
    void testGetOrderItems_Success() throws Exception {
        // Given
        Integer orderId = 1;
        OrderItemDTO item1 = new OrderItemDTO(
            101,
            "Pho Bo",
            new BigDecimal("50000"),
            10,
            "pho-bo.jpg",
            2
        );
        OrderItemDTO item2 = new OrderItemDTO(
            102,
            "Banh Mi",
            new BigDecimal("25000"),
            15,
            "banh-mi.jpg",
            3
        );
        List<OrderItemDTO> expectedItems = List.of(item1, item2);

        // Mock order-service response
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(expectedItems))));

        // When
        List<OrderItemDTO> actualItems = orderClient.getOrderItems(orderId);

        // Then
        assertNotNull(actualItems);
        assertEquals(2, actualItems.size());
        
        // Verify first item
        OrderItemDTO actualItem1 = actualItems.get(0);
        assertEquals(101, actualItem1.getMenuItemId());
        assertEquals("Pho Bo", actualItem1.getName());
        assertEquals(new BigDecimal("50000"), actualItem1.getPrice());
        assertEquals(10, actualItem1.getStock());
        assertEquals("pho-bo.jpg", actualItem1.getImageUrl());
        assertEquals(2, actualItem1.getQuantity());
        
        // Verify second item
        OrderItemDTO actualItem2 = actualItems.get(1);
        assertEquals(102, actualItem2.getMenuItemId());
        assertEquals("Banh Mi", actualItem2.getName());
        assertEquals(new BigDecimal("25000"), actualItem2.getPrice());
        assertEquals(15, actualItem2.getStock());
        assertEquals("banh-mi.jpg", actualItem2.getImageUrl());
        assertEquals(3, actualItem2.getQuantity());

        // Verify WireMock was called
        verify(1, getRequestedFor(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items")));
    }

    @Test
    @Order(2)
    @DisplayName("Should return empty list when order has no items")
    void testGetOrderItems_EmptyList() throws Exception {
        // Given
        Integer orderId = 2;
        List<OrderItemDTO> emptyList = List.of();

        // Mock order-service response
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(emptyList))));

        // When
        List<OrderItemDTO> actualItems = orderClient.getOrderItems(orderId);

        // Then
        assertNotNull(actualItems);
        assertTrue(actualItems.isEmpty());
        
        // Verify WireMock was called
        verify(1, getRequestedFor(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items")));
    }

    @Test
    @Order(3)
    @DisplayName("Should handle 404 when order is not found")
    void testGetOrderItems_OrderNotFound() {
        // Given
        Integer orderId = 999;

        // Mock order-service 404 response
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"error\": \"Order not found\"}")));

        // When & Then
        assertThrows(Exception.class, () -> {
            orderClient.getOrderItems(orderId);
        });

        // Verify WireMock was called
        verify(1, getRequestedFor(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items")));
    }

    @Test
    @Order(4)
    @DisplayName("Should handle 500 internal server error from order-service")
    void testGetOrderItems_ServerError() {
        // Given
        Integer orderId = 1;

        // Mock order-service 500 response
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"error\": \"Internal server error\"}")));

        // When & Then
        assertThrows(Exception.class, () -> {
            orderClient.getOrderItems(orderId);
        });

        // Verify WireMock was called
        verify(1, getRequestedFor(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items")));
    }

    @Test
    @Order(5)
    @DisplayName("Should retrieve order items with different quantities")
    void testGetOrderItems_DifferentQuantities() throws Exception {
        // Given
        Integer orderId = 3;
        OrderItemDTO item1 = new OrderItemDTO(
            201,
            "Com Tam",
            new BigDecimal("45000"),
            20,
            "com-tam.jpg",
            1
        );
        OrderItemDTO item2 = new OrderItemDTO(
            202,
            "Bun Cha",
            new BigDecimal("55000"),
            5,
            "bun-cha.jpg",
            5
        );
        List<OrderItemDTO> expectedItems = List.of(item1, item2);

        // Mock order-service response
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(expectedItems))));

        // When
        List<OrderItemDTO> actualItems = orderClient.getOrderItems(orderId);

        // Then
        assertNotNull(actualItems);
        assertEquals(2, actualItems.size());
        assertEquals(1, actualItems.get(0).getQuantity());
        assertEquals(5, actualItems.get(1).getQuantity());
        
        // Verify WireMock was called
        verify(1, getRequestedFor(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items")));
    }

    @Test
    @Order(6)
    @DisplayName("Should handle order with single item")
    void testGetOrderItems_SingleItem() throws Exception {
        // Given
        Integer orderId = 4;
        OrderItemDTO singleItem = new OrderItemDTO(
            301,
            "Cafe Sua Da",
            new BigDecimal("20000"),
            50,
            "cafe.jpg",
            1
        );
        List<OrderItemDTO> expectedItems = List.of(singleItem);

        // Mock order-service response
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(expectedItems))));

        // When
        List<OrderItemDTO> actualItems = orderClient.getOrderItems(orderId);

        // Then
        assertNotNull(actualItems);
        assertEquals(1, actualItems.size());
        assertEquals(301, actualItems.get(0).getMenuItemId());
        assertEquals("Cafe Sua Da", actualItems.get(0).getName());
        
        // Verify WireMock was called
        verify(1, getRequestedFor(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items")));
    }

    @Test
    @Order(7)
    @DisplayName("Should handle timeout from order-service")
    void testGetOrderItems_Timeout() {
        // Given
        Integer orderId = 5;

        // Mock order-service timeout (delay longer than client timeout)
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withFixedDelay(60000) // 60 second delay
                .withBody("[]")));

        // When & Then
        assertThrows(Exception.class, () -> {
            orderClient.getOrderItems(orderId);
        });
    }

    @Test
    @Order(8)
    @DisplayName("Should verify correct endpoint is called")
    void testGetOrderItems_CorrectEndpoint() throws Exception {
        // Given
        Integer orderId = 123;
        List<OrderItemDTO> emptyList = List.of();

        // Mock order-service response
        stubFor(get(urlEqualTo(ORDER_SERVICE_BASE_PATH + "/" + orderId + "/items"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(emptyList))));

        // When
        orderClient.getOrderItems(orderId);

        // Then - Verify exact endpoint was called
        verify(1, getRequestedFor(urlEqualTo("/api/v1/orders/123/items")));
        verify(0, getRequestedFor(urlMatching("/api/v1/orders/[0-9]+/items/.*")));
    }
}

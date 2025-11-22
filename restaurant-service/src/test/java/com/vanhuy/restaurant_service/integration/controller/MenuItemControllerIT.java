package com.vanhuy.restaurant_service.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.restaurant_service.dto.OrderItemDTO;
import com.vanhuy.restaurant_service.model.MenuItem;
import com.vanhuy.restaurant_service.model.Restaurant;
import com.vanhuy.restaurant_service.repository.MenuItemRepository;
import com.vanhuy.restaurant_service.repository.RestaurantRepository;
import com.vanhuy.restaurant_service.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MenuItemControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private static final String BASE_URL = "/api/v1/menu-items";
    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir") + "/test-uploads";

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        // Clean database
        menuItemRepository.deleteAll();
        restaurantRepository.deleteAll();

        // Create test restaurant
        testRestaurant = new Restaurant();
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("Test Address");
        testRestaurant = restaurantRepository.save(testRestaurant);
    }

    @AfterEach
    void tearDown() {
        // Clean up uploaded files
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (uploadDir.exists()) {
                File[] files = uploadDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().startsWith("test-")) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    @Order(1)
    @Transactional
    void testCreateMenuItem_Success() throws Exception {
        // Given
        OrderItemDTO requestDTO = new OrderItemDTO(
                null,
                "Pho Bo",
                new BigDecimal("50000"),
                20,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post(BASE_URL + "/{restaurantId}", testRestaurant.getRestaurantId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuItemId").exists())
                .andExpect(jsonPath("$.name").value("Pho Bo"))
                .andExpect(jsonPath("$.price").value(50000))
                .andExpect(jsonPath("$.stock").value(20));

        // Verify in database
        assertEquals(1, menuItemRepository.count());
        MenuItem saved = menuItemRepository.findAll().get(0);
        assertEquals("Pho Bo", saved.getName());
        assertEquals(new BigDecimal("50000"), saved.getPrice());
    }

    @Test
    @Order(2)
    @Transactional
    void testCreateMenuItem_RestaurantNotFound() throws Exception {
        // Given
        Integer nonExistentRestaurantId = 999;
        OrderItemDTO requestDTO = new OrderItemDTO(
                null,
                "Pho Bo",
                new BigDecimal("50000"),
                20,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post(BASE_URL + "/{restaurantId}", nonExistentRestaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        // Verify not saved in database
        assertEquals(0, menuItemRepository.count());
    }

    @Test
    @Order(3)
    @Transactional
    void testGetAllMenuItems_Success() throws Exception {
        // Given - Create multiple menu items
        MenuItem item1 = new MenuItem();
        item1.setName("Pho Bo");
        item1.setPrice(new BigDecimal("50000"));
        item1.setStock(20);
        item1.setRestaurant(testRestaurant);
        menuItemRepository.save(item1);

        MenuItem item2 = new MenuItem();
        item2.setName("Bun Cha");
        item2.setPrice(new BigDecimal("45000"));
        item2.setStock(15);
        item2.setRestaurant(testRestaurant);
        menuItemRepository.save(item2);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/restaurant/{restaurantId}", testRestaurant.getRestaurantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Pho Bo", "Bun Cha")));
    }

    @Test
    @Order(4)
    @Transactional
    void testGetAllMenuItems_RestaurantNotFound() throws Exception {
        // Given
        Integer nonExistentRestaurantId = 999;

        // When & Then
        mockMvc.perform(get(BASE_URL + "/restaurant/{restaurantId}", nonExistentRestaurantId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @Transactional
    void testGetAllMenuItems_EmptyResult() throws Exception {
        // Given - No menu items for restaurant

        // When & Then
        mockMvc.perform(get(BASE_URL + "/restaurant/{restaurantId}", testRestaurant.getRestaurantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(6)
    @Transactional
    void testUploadMenuItemImage_Success() throws Exception {
        // Given
        MenuItem menuItem = new MenuItem();
        menuItem.setName("Test Item");
        menuItem.setPrice(new BigDecimal("50000"));
        menuItem.setStock(10);
        menuItem.setRestaurant(testRestaurant);
        MenuItem saved = menuItemRepository.save(menuItem);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-menu-item.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart(BASE_URL + "/{menuItemId}/upload-image", saved.getItemId())
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuItemId").value(saved.getItemId()))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.imageUrl").value(containsString(".jpg")));

        // Verify in database
        MenuItem updated = menuItemRepository.findById(saved.getItemId()).orElseThrow();
        assertNotNull(updated.getImageUrl());
    }

    @Test
    @Order(7)
    @Transactional
    void testUploadMenuItemImage_MenuItemNotFound() throws Exception {
        // Given
        Integer nonExistentMenuItemId = 999;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart(BASE_URL + "/{menuItemId}/upload-image", nonExistentMenuItemId)
                        .file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @Transactional
    void testGetMenuItemImage_Success() throws Exception {
        // Given - Create a test image file
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String testFileName = "test-menu-item-" + System.currentTimeMillis() + ".jpg";
        Path testFilePath = uploadPath.resolve(testFileName);
        byte[] testContent = "test menu item image content".getBytes();
        Files.write(testFilePath, testContent);

        try {
            // When & Then
            mockMvc.perform(get(BASE_URL + "/images/{filename}", testFileName))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                    .andExpect(header().exists("Content-Disposition"))
                    .andExpect(header().string("Cache-Control", "max-age=31536000"))
                    .andExpect(content().bytes(testContent));
        } finally {
            // Cleanup
            Files.deleteIfExists(testFilePath);
        }
    }

    @Test
    @Order(9)
    @Transactional
    void testCreateMenuItem_ThenRetrieve() throws Exception {
        // Given
        OrderItemDTO requestDTO = new OrderItemDTO(
                null,
                "Integration Test Item",
                new BigDecimal("75000"),
                15,
                null,
                null
        );

        // When - Create menu item
        String responseJson = mockMvc.perform(post(BASE_URL + "/{restaurantId}", testRestaurant.getRestaurantId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderItemDTO created = objectMapper.readValue(responseJson, OrderItemDTO.class);

        // Then - Retrieve and verify
        mockMvc.perform(get(BASE_URL + "/restaurant/{restaurantId}", testRestaurant.getRestaurantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].menuItemId").value(created.getMenuItemId()))
                .andExpect(jsonPath("$[0].name").value("Integration Test Item"))
                .andExpect(jsonPath("$[0].price").value(75000));
    }

    @Test
    @Order(10)
    @Transactional
    void testGetMenuItemsFromDifferentRestaurants() throws Exception {
        // Given - Create another restaurant
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Restaurant 2");
        restaurant2.setAddress("Address 2");
        restaurant2 = restaurantRepository.save(restaurant2);

        // Create menu items for both restaurants
        MenuItem item1 = new MenuItem();
        item1.setName("Item Restaurant 1");
        item1.setPrice(new BigDecimal("50000"));
        item1.setStock(10);
        item1.setRestaurant(testRestaurant);
        menuItemRepository.save(item1);

        MenuItem item2 = new MenuItem();
        item2.setName("Item Restaurant 2");
        item2.setPrice(new BigDecimal("60000"));
        item2.setStock(8);
        item2.setRestaurant(restaurant2);
        menuItemRepository.save(item2);

        // When & Then - Get items from restaurant 1
        mockMvc.perform(get(BASE_URL + "/restaurant/{restaurantId}", testRestaurant.getRestaurantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Item Restaurant 1"));

        // Get items from restaurant 2
        mockMvc.perform(get(BASE_URL + "/restaurant/{restaurantId}", restaurant2.getRestaurantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Item Restaurant 2"));
    }
}

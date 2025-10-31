// package com.vanhuy.restaurant_service.integration.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.vanhuy.restaurant_service.dto.MenuItemDTO;
// import com.vanhuy.restaurant_service.model.MenuItem;
// import com.vanhuy.restaurant_service.model.Restaurant;
// import com.vanhuy.restaurant_service.repository.MenuItemRepository;
// import com.vanhuy.restaurant_service.repository.RestaurantRepository;
// import org.junit.jupiter.api.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.transaction.annotation.Transactional;

// import java.io.File;
// import java.math.BigDecimal;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.List;

// import static org.hamcrest.Matchers.*;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// class MenuItemControllerIT {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private MenuItemRepository menuItemRepository;

//     @Autowired
//     private RestaurantRepository restaurantRepository;

//     private static final String BASE_URL = "/api/v1/restaurants";
//     private static final String UPLOAD_DIR = "src/main/resources/uploads";

//     private Restaurant testRestaurant;

//     @BeforeEach
//     void setUp() {
//         // Clean database
//         menuItemRepository.deleteAll();
//         restaurantRepository.deleteAll();

//         // Create test restaurant
//         testRestaurant = new Restaurant();
//         testRestaurant.setName("Test Restaurant");
//         testRestaurant.setAddress("Test Address");
//         testRestaurant = restaurantRepository.save(testRestaurant);
//     }

//     @AfterEach
//     void tearDown() {
//         // Clean up uploaded files
//         try {
//             File uploadDir = new File(UPLOAD_DIR);
//             if (uploadDir.exists()) {
//                 File[] files = uploadDir.listFiles();
//                 if (files != null) {
//                     for (File file : files) {
//                         if (file.getName().startsWith("test-")) {
//                             file.delete();
//                         }
//                     }
//                 }
//             }
//         } catch (Exception e) {
//             // Ignore cleanup errors
//         }
//     }

//     @Test
//     @Order(1)
//     @Transactional
//     void testCreateMenuItem_Success() throws Exception {
//         // Given
//         MenuItemDTO requestDTO = new MenuItemDTO(
//                 null,
//                 "Pho Bo",
//                 "Traditional Vietnamese beef noodle soup",
//                 new BigDecimal("50000"),
//                 null,
//                 testRestaurant.getRestaurantId()
//         );

//         // When & Then
//         mockMvc.perform(post(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(requestDTO)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.menuItemId").exists())
//                 .andExpect(jsonPath("$.name").value("Pho Bo"))
//                 .andExpect(jsonPath("$.description").value("Traditional Vietnamese beef noodle soup"))
//                 .andExpect(jsonPath("$.price").value(50000))
//                 .andExpect(jsonPath("$.restaurantId").value(testRestaurant.getRestaurantId()));

//         // Verify in database
//         assertEquals(1, menuItemRepository.count());
//         MenuItem saved = menuItemRepository.findAll().get(0);
//         assertEquals("Pho Bo", saved.getName());
//         assertEquals(new BigDecimal("50000"), saved.getPrice());
//     }

//     @Test
//     @Order(2)
//     @Transactional
//     void testCreateMenuItem_RestaurantNotFound() throws Exception {
//         // Given
//         Integer nonExistentRestaurantId = 999;
//         MenuItemDTO requestDTO = new MenuItemDTO(
//                 null,
//                 "Pho Bo",
//                 "Description",
//                 new BigDecimal("50000"),
//                 null,
//                 nonExistentRestaurantId
//         );

//         // When & Then
//         mockMvc.perform(post(BASE_URL + "/{restaurantId}/menu-items", nonExistentRestaurantId)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(requestDTO)))
//                 .andExpect(status().isNotFound());

//         // Verify not saved in database
//         assertEquals(0, menuItemRepository.count());
//     }

//     @Test
//     @Order(3)
//     @Transactional
//     void testGetAllMenuItems_Success() throws Exception {
//         // Given - Create multiple menu items
//         MenuItem item1 = new MenuItem();
//         item1.setName("Pho Bo");
//         item1.setDescription("Beef noodle soup");
//         item1.setPrice(new BigDecimal("50000"));
//         item1.setRestaurant(testRestaurant);
//         menuItemRepository.save(item1);

//         MenuItem item2 = new MenuItem();
//         item2.setName("Bun Cha");
//         item2.setDescription("Grilled pork with noodles");
//         item2.setPrice(new BigDecimal("45000"));
//         item2.setRestaurant(testRestaurant);
//         menuItemRepository.save(item2);

//         // When & Then
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(2)))
//                 .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Pho Bo", "Bun Cha")))
//                 .andExpect(jsonPath("$.totalElements").value(2));
//     }

//     @Test
//     @Order(4)
//     @Transactional
//     void testGetAllMenuItems_WithKeyword() throws Exception {
//         // Given
//         MenuItem item1 = new MenuItem();
//         item1.setName("Pho Bo");
//         item1.setDescription("Beef noodle soup");
//         item1.setPrice(new BigDecimal("50000"));
//         item1.setRestaurant(testRestaurant);
//         menuItemRepository.save(item1);

//         MenuItem item2 = new MenuItem();
//         item2.setName("Pho Ga");
//         item2.setDescription("Chicken noodle soup");
//         item2.setPrice(new BigDecimal("45000"));
//         item2.setRestaurant(testRestaurant);
//         menuItemRepository.save(item2);

//         MenuItem item3 = new MenuItem();
//         item3.setName("Bun Cha");
//         item3.setDescription("Grilled pork");
//         item3.setPrice(new BigDecimal("40000"));
//         item3.setRestaurant(testRestaurant);
//         menuItemRepository.save(item3);

//         // When & Then - Search for "Pho"
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("keyword", "Pho")
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(2)))
//                 .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Pho Bo", "Pho Ga")))
//                 .andExpect(jsonPath("$.totalElements").value(2));
//     }

//     @Test
//     @Order(5)
//     @Transactional
//     void testGetAllMenuItems_RestaurantNotFound() throws Exception {
//         // Given
//         Integer nonExistentRestaurantId = 999;

//         // When & Then
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", nonExistentRestaurantId)
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     @Order(6)
//     @Transactional
//     void testGetAllMenuItems_EmptyResult() throws Exception {
//         // Given - No menu items for restaurant

//         // When & Then
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(0)))
//                 .andExpect(jsonPath("$.totalElements").value(0));
//     }

//     @Test
//     @Order(7)
//     @Transactional
//     void testGetAllMenuItems_Pagination() throws Exception {
//         // Given - Create 15 menu items
//         for (int i = 1; i <= 15; i++) {
//             MenuItem item = new MenuItem();
//             item.setName("Item " + i);
//             item.setDescription("Description " + i);
//             item.setPrice(new BigDecimal(i * 10000));
//             item.setRestaurant(testRestaurant);
//             menuItemRepository.save(item);
//         }

//         // When & Then - First page
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("page", "0")
//                         .param("size", "5"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(5)))
//                 .andExpect(jsonPath("$.totalElements").value(15))
//                 .andExpect(jsonPath("$.totalPages").value(3));

//         // Second page
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("page", "1")
//                         .param("size", "5"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(5)));
//     }

//     @Test
//     @Order(8)
//     @Transactional
//     void testUploadMenuItemImage_Success() throws Exception {
//         // Given
//         MenuItem menuItem = new MenuItem();
//         menuItem.setName("Test Item");
//         menuItem.setDescription("Test Description");
//         menuItem.setPrice(new BigDecimal("50000"));
//         menuItem.setRestaurant(testRestaurant);
//         MenuItem saved = menuItemRepository.save(menuItem);

//         MockMultipartFile file = new MockMultipartFile(
//                 "file",
//                 "test-menu-item.jpg",
//                 MediaType.IMAGE_JPEG_VALUE,
//                 "test image content".getBytes()
//         );

//         // When & Then
//         mockMvc.perform(multipart(BASE_URL + "/{restaurantId}/menu-items/{menuItemId}/upload-image",
//                                 testRestaurant.getRestaurantId(), saved.getMenuItemId())
//                         .file(file))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.menuItemId").value(saved.getMenuItemId()))
//                 .andExpect(jsonPath("$.name").value("Test Item"))
//                 .andExpect(jsonPath("$.image").exists())
//                 .andExpect(jsonPath("$.image").value(containsString(".jpg")));

//         // Verify in database
//         MenuItem updated = menuItemRepository.findById(saved.getMenuItemId()).orElseThrow();
//         assertNotNull(updated.getImage());
//     }

//     @Test
//     @Order(9)
//     @Transactional
//     void testUploadMenuItemImage_MenuItemNotFound() throws Exception {
//         // Given
//         Integer nonExistentMenuItemId = 999;
//         MockMultipartFile file = new MockMultipartFile(
//                 "file",
//                 "test-image.jpg",
//                 MediaType.IMAGE_JPEG_VALUE,
//                 "test image content".getBytes()
//         );

//         // When & Then
//         mockMvc.perform(multipart(BASE_URL + "/{restaurantId}/menu-items/{menuItemId}/upload-image",
//                                 testRestaurant.getRestaurantId(), nonExistentMenuItemId)
//                         .file(file))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     @Order(10)
//     @Transactional
//     void testGetMenuItemImage_Success() throws Exception {
//         // Given - Create a test image file
//         Path uploadPath = Paths.get(UPLOAD_DIR);
//         if (!Files.exists(uploadPath)) {
//             Files.createDirectories(uploadPath);
//         }

//         String testFileName = "test-menu-item-" + System.currentTimeMillis() + ".jpg";
//         Path testFilePath = uploadPath.resolve(testFileName);
//         byte[] testContent = "test menu item image content".getBytes();
//         Files.write(testFilePath, testContent);

//         try {
//             // When & Then
//             mockMvc.perform(get(BASE_URL + "/menu-items/images/{filename}", testFileName))
//                     .andExpect(status().isOk())
//                     .andExpect(content().contentType(MediaType.IMAGE_JPEG))
//                     .andExpect(header().exists("Content-Disposition"))
//                     .andExpect(header().string("Cache-Control", "max-age=31536000"))
//                     .andExpect(content().bytes(testContent));
//         } finally {
//             // Cleanup
//             Files.deleteIfExists(testFilePath);
//         }
//     }

//     @Test
//     @Order(11)
//     @Transactional
//     void testCreateMenuItem_ThenRetrieve() throws Exception {
//         // Given
//         MenuItemDTO requestDTO = new MenuItemDTO(
//                 null,
//                 "Integration Test Item",
//                 "Integration Test Description",
//                 new BigDecimal("75000"),
//                 null,
//                 testRestaurant.getRestaurantId()
//         );

//         // When - Create menu item
//         String responseJson = mockMvc.perform(post(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(requestDTO)))
//                 .andExpect(status().isCreated())
//                 .andReturn()
//                 .getResponse()
//                 .getContentAsString();

//         MenuItemDTO created = objectMapper.readValue(responseJson, MenuItemDTO.class);

//         // Then - Retrieve and verify
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("keyword", "Integration Test")
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(1)))
//                 .andExpect(jsonPath("$.content[0].menuItemId").value(created.menuItemId()))
//                 .andExpect(jsonPath("$.content[0].name").value("Integration Test Item"))
//                 .andExpect(jsonPath("$.content[0].price").value(75000));
//     }

//     @Test
//     @Order(12)
//     @Transactional
//     void testSearchByDescription() throws Exception {
//         // Given
//         MenuItem item1 = new MenuItem();
//         item1.setName("Pho");
//         item1.setDescription("Traditional Vietnamese beef soup");
//         item1.setPrice(new BigDecimal("50000"));
//         item1.setRestaurant(testRestaurant);
//         menuItemRepository.save(item1);

//         MenuItem item2 = new MenuItem();
//         item2.setName("Bun Bo");
//         item2.setDescription("Spicy beef noodle soup");
//         item2.setPrice(new BigDecimal("45000"));
//         item2.setRestaurant(testRestaurant);
//         menuItemRepository.save(item2);

//         // When & Then - Search by description keyword
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("keyword", "soup")
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(2)));
//     }

//     @Test
//     @Order(13)
//     @Transactional
//     void testGetMenuItemsFromDifferentRestaurants() throws Exception {
//         // Given - Create another restaurant
//         Restaurant restaurant2 = new Restaurant();
//         restaurant2.setName("Restaurant 2");
//         restaurant2.setAddress("Address 2");
//         restaurant2 = restaurantRepository.save(restaurant2);

//         // Create menu items for both restaurants
//         MenuItem item1 = new MenuItem();
//         item1.setName("Item Restaurant 1");
//         item1.setDescription("Description 1");
//         item1.setPrice(new BigDecimal("50000"));
//         item1.setRestaurant(testRestaurant);
//         menuItemRepository.save(item1);

//         MenuItem item2 = new MenuItem();
//         item2.setName("Item Restaurant 2");
//         item2.setDescription("Description 2");
//         item2.setPrice(new BigDecimal("60000"));
//         item2.setRestaurant(restaurant2);
//         menuItemRepository.save(item2);

//         // When & Then - Get items from restaurant 1
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", testRestaurant.getRestaurantId())
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(1)))
//                 .andExpect(jsonPath("$.content[0].name").value("Item Restaurant 1"));

//         // Get items from restaurant 2
//         mockMvc.perform(get(BASE_URL + "/{restaurantId}/menu-items", restaurant2.getRestaurantId())
//                         .param("page", "0")
//                         .param("size", "10"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content", hasSize(1)))
//                 .andExpect(jsonPath("$.content[0].name").value("Item Restaurant 2"));
//     }
// }

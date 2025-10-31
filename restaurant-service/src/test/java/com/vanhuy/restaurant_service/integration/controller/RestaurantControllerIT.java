package com.vanhuy.restaurant_service.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.restaurant_service.dto.RestaurantDTO;
import com.vanhuy.restaurant_service.model.Restaurant;
import com.vanhuy.restaurant_service.repository.RestaurantRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestaurantControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private static final String BASE_URL = "/api/v1/restaurants";
    private static final String UPLOAD_DIR = "src/main/resources/uploads";

    @BeforeEach
    void setUp() {
        // Clean database before each test
        restaurantRepository.deleteAll();
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
    void testCreateRestaurant_Success() throws Exception {
        // Given
        RestaurantDTO requestDTO = new RestaurantDTO(
                null,
                "Pho 24",
                "123 Nguyen Hue, District 1, HCMC",
                null
        );

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantId").exists())
                .andExpect(jsonPath("$.name").value("Pho 24"))
                .andExpect(jsonPath("$.address").value("123 Nguyen Hue, District 1, HCMC"));

        // Verify in database
        assertEquals(1, restaurantRepository.count());
        Restaurant saved = restaurantRepository.findAll().get(0);
        assertEquals("Pho 24", saved.getName());
        assertEquals("123 Nguyen Hue, District 1, HCMC", saved.getAddress());
    }

    @Test
    @Order(2)
    @Transactional
    void testCreateRestaurant_WithSpecialCharacters() throws Exception {
        // Given
        RestaurantDTO requestDTO = new RestaurantDTO(
                null,
                "Phở Hà Nội & Bún Chả",
                "Số 10, Đường Nguyễn Huệ, Quận 1, TP.HCM",
                null
        );

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Phở Hà Nội & Bún Chả"))
                .andExpect(jsonPath("$.address").value("Số 10, Đường Nguyễn Huệ, Quận 1, TP.HCM"));

        // Verify in database
        Restaurant saved = restaurantRepository.findAll().get(0);
        assertEquals("Phở Hà Nội & Bún Chả", saved.getName());
    }

    @Test
    @Order(3)
    @Transactional
    void testGetAllRestaurants_WithoutKeyword() throws Exception {
        // Given
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Pho 24");
        restaurant1.setAddress("123 Nguyen Hue");
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Bun Cha Hanoi");
        restaurant2.setAddress("456 Le Loi");
        restaurantRepository.save(restaurant2);

        Restaurant restaurant3 = new Restaurant();
        restaurant3.setName("Com Tam Suon");
        restaurant3.setAddress("789 Tran Hung Dao");
        restaurantRepository.save(restaurant3);

        // When & Then
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Pho 24", "Bun Cha Hanoi", "Com Tam Suon")))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @Order(4)
    @Transactional
    void testGetAllRestaurants_WithKeyword() throws Exception {
        // Given
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Pho 24");
        restaurant1.setAddress("123 Nguyen Hue");
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Pho Thin");
        restaurant2.setAddress("456 Le Loi");
        restaurantRepository.save(restaurant2);

        Restaurant restaurant3 = new Restaurant();
        restaurant3.setName("Bun Cha Hanoi");
        restaurant3.setAddress("789 Tran Hung Dao");
        restaurantRepository.save(restaurant3);

        // When & Then - Search for "Pho"
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "Pho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Pho 24", "Pho Thin")))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @Order(5)
    @Transactional
    void testGetAllRestaurants_SearchByAddress() throws Exception {
        // Given
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Restaurant A");
        restaurant1.setAddress("District 1, HCMC");
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Restaurant B");
        restaurant2.setAddress("District 2, HCMC");
        restaurantRepository.save(restaurant2);

        Restaurant restaurant3 = new Restaurant();
        restaurant3.setName("Restaurant C");
        restaurant3.setAddress("District 1, Hanoi");
        restaurantRepository.save(restaurant3);

        // When & Then - Search by address
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "District 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @Order(6)
    @Transactional
    void testGetAllRestaurants_EmptyResult() throws Exception {
        // Given - No restaurants in database

        // When & Then
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @Order(7)
    @Transactional
    void testGetAllRestaurants_Pagination() throws Exception {
        // Given - Create 15 restaurants
        for (int i = 1; i <= 15; i++) {
            Restaurant restaurant = new Restaurant();
            restaurant.setName("Restaurant " + i);
            restaurant.setAddress("Address " + i);
            restaurantRepository.save(restaurant);
        }

        // When & Then - First page (size=5)
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        // Second page
        mockMvc.perform(get(BASE_URL)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));

        // Last page
        mockMvc.perform(get(BASE_URL)
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @Order(8)
    @Transactional
    void testGetAllRestaurants_CaseInsensitiveSearch() throws Exception {
        // Given
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("PHO 24");
        restaurant1.setAddress("123 Nguyen Hue");
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("pho thin");
        restaurant2.setAddress("456 Le Loi");
        restaurantRepository.save(restaurant2);

        // When & Then - Search with lowercase
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "pho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // Search with uppercase
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "PHO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // Search with mixed case
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "PhO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @Order(9)
    @Transactional
    void testUploadImage_Success() throws Exception {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("Test Address");
        Restaurant saved = restaurantRepository.save(restaurant);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-restaurant.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart(BASE_URL + "/{restaurantId}/upload-image", saved.getRestaurantId())
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(saved.getRestaurantId()))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.image").exists())
                .andExpect(jsonPath("$.image").value(containsString(".jpg")));

        // Verify in database
        Restaurant updated = restaurantRepository.findById(saved.getRestaurantId()).orElseThrow();
        assertNotNull(updated.getImage());
        assertTrue(updated.getImage().contains(".jpg"));
    }

    @Test
    @Order(10)
    @Transactional
    void testUploadImage_RestaurantNotFound() throws Exception {
        // Given
        Integer nonExistentId = 999;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-restaurant.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart(BASE_URL + "/{restaurantId}/upload-image", nonExistentId)
                        .file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @Transactional
    void testUploadImage_UpdateExistingImage() throws Exception {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurant.setImage("old-image.jpg");
        Restaurant saved = restaurantRepository.save(restaurant);

        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "test-new-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart(BASE_URL + "/{restaurantId}/upload-image", saved.getRestaurantId())
                        .file(newFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value(not(containsString("old-image.jpg"))));

        // Verify in database
        Restaurant updated = restaurantRepository.findById(saved.getRestaurantId()).orElseThrow();
        assertNotEquals("old-image.jpg", updated.getImage());
    }

    @Test
    @Order(12)
    @Transactional
    void testGetImage_Success() throws Exception {
        // Given - Create a test image file
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String testFileName = "test-image-" + System.currentTimeMillis() + ".jpg";
        Path testFilePath = uploadPath.resolve(testFileName);
        byte[] testContent = "test image content".getBytes();
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
    @Order(13)
    @Transactional
    void testGetAllRestaurants_NoKeywordParameter() throws Exception {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurantRepository.save(restaurant);

        // When & Then - Without keyword parameter at all
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @Order(14)
    @Transactional
    void testGetAllRestaurants_EmptyKeyword() throws Exception {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurantRepository.save(restaurant);

        // When & Then - With empty keyword
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // With whitespace keyword
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @Order(15)
    @Transactional
    void testSearchRestaurants_PartialMatch() throws Exception {
        // Given
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Pho 24 Nguyen Hue");
        restaurant1.setAddress("District 1");
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Pho Thin Bo Ho");
        restaurant2.setAddress("District 3");
        restaurantRepository.save(restaurant2);

        Restaurant restaurant3 = new Restaurant();
        restaurant3.setName("Bun Bo Hue");
        restaurant3.setAddress("District 5");
        restaurantRepository.save(restaurant3);

        // When & Then - Search "Pho" should match 2 restaurants
        mockMvc.perform(get(BASE_URL)
                        .param("keyword", "Pho")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", hasItem(containsString("Pho"))));

        // Search "Bo" should match 2 restaurants
        mockMvc.perform(get(BASE_URL)
                        .param("keyword", "Bo")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @Order(16)
    @Transactional
    void testCreateRestaurant_ThenRetrieve() throws Exception {
        // Given
        RestaurantDTO requestDTO = new RestaurantDTO(
                null,
                "Integration Test Restaurant",
                "Integration Test Address",
                null
        );

        // When - Create restaurant
        String responseJson = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RestaurantDTO createdRestaurant = objectMapper.readValue(responseJson, RestaurantDTO.class);

        // Then - Retrieve and verify
        mockMvc.perform(get(BASE_URL)
                        .param("keyword", "Integration Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].restaurantId").value(createdRestaurant.restaurantId()))
                .andExpect(jsonPath("$.content[0].name").value("Integration Test Restaurant"))
                .andExpect(jsonPath("$.content[0].address").value("Integration Test Address"));
    }

    @Test
    @Order(17)
    @Transactional
    void testUploadImage_ThenRetrieveRestaurant() throws Exception {
        // Given - Create restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("Test Address");
        Restaurant saved = restaurantRepository.save(restaurant);

        // When - Upload image
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart(BASE_URL + "/{restaurantId}/upload-image", saved.getRestaurantId())
                        .file(file))
                .andExpect(status().isOk());

        // Then - Retrieve restaurant and verify image exists
        mockMvc.perform(get(BASE_URL)
                        .param("keyword", "Test Restaurant")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].image").exists())
                .andExpect(jsonPath("$.content[0].image").isNotEmpty());
    }
}

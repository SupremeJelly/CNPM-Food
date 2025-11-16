package com.vanhuy.restaurant_service.unit.service;

import com.vanhuy.restaurant_service.dto.RestaurantDTO;
import com.vanhuy.restaurant_service.exception.RestaurantNotFoundException;
import com.vanhuy.restaurant_service.model.Restaurant;
import com.vanhuy.restaurant_service.repository.RestaurantRepository;
import com.vanhuy.restaurant_service.service.FileStorageService;
import com.vanhuy.restaurant_service.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant restaurant;
    private RestaurantDTO restaurantDTO;

    @BeforeEach
    void setUp() {

        restaurant = new Restaurant();
        restaurant.setRestaurantId(1);
        restaurant.setName("Pho 24");
        restaurant.setAddress("123 Nguyen Hue, District 1, HCMC");
        restaurant.setImage("pho24.jpg");

        restaurantDTO = new RestaurantDTO(
                1,
                "Pho 24",
                "123 Nguyen Hue, District 1, HCMC",
                "pho24.jpg"
        );
    }

    @Test
    void testCreateRestaurantSuccess() {
        // Given
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        RestaurantDTO result = restaurantService.createRestaurant(restaurantDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.restaurantId()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("Pho 24");
        assertThat(result.address()).isEqualTo("123 Nguyen Hue, District 1, HCMC");
        
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testGetAllRestaurantsSuccess() {
        // Given
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setRestaurantId(2);
        restaurant2.setName("Bun Cha Hanoi");
        restaurant2.setAddress("456 Le Loi, District 1, HCMC");
        restaurant2.setImage("buncha.jpg");

        List<Restaurant> restaurants = Arrays.asList(restaurant, restaurant2);
        when(restaurantRepository.findAll()).thenReturn(restaurants);

        // When
        List<RestaurantDTO> result = restaurantService.getAllRestaurants();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Pho 24");
        assertThat(result.get(1).name()).isEqualTo("Bun Cha Hanoi");
        
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void testGetAllRestaurantsEmpty() {
        // Given
        when(restaurantRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<RestaurantDTO> result = restaurantService.getAllRestaurants();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void testUploadImageSuccess() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        String oldImageFileName = "old-image.jpg";
        String newImageFileName = "new-image.jpg";
        
        restaurant.setImage(oldImageFileName);
        
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(fileStorageService.uploadImage(mockFile, oldImageFileName)).thenReturn(newImageFileName);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        RestaurantDTO result = restaurantService.uploadImage(1, mockFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.image()).contains(newImageFileName);
        
        verify(restaurantRepository, times(1)).findById(1);
        verify(fileStorageService, times(1)).uploadImage(mockFile, oldImageFileName);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testUploadImageRestaurantNotFound() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(restaurantRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> restaurantService.uploadImage(999, mockFile))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("Restaurant not found");

        verify(restaurantRepository, times(1)).findById(999);
        verify(fileStorageService, never()).uploadImage(any(), any());
    }

    @Test
    void testGetRestaurantsByPageSuccess() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Restaurant> restaurants = Arrays.asList(restaurant);
        Page<Restaurant> restaurantPage = new PageImpl<>(restaurants, pageable, 1);
        
        when(restaurantRepository.findAll(pageable)).thenReturn(restaurantPage);

        // When
        Page<RestaurantDTO> result = restaurantService.getRestaurantsByPage(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Pho 24");
        
        verify(restaurantRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetRestaurantsByPageEmpty() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(restaurantRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<RestaurantDTO> result = restaurantService.getRestaurantsByPage(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        
        verify(restaurantRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetRestaurantByIdSuccess() {
        // Given
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        // When
        Restaurant result = restaurantService.getRestaurantById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Pho 24");
        
        verify(restaurantRepository, times(1)).findById(1);
    }

    @Test
    void testGetRestaurantByIdNotFound() {
        // Given
        when(restaurantRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> restaurantService.getRestaurantById(999))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("Restaurant not found");

        verify(restaurantRepository, times(1)).findById(999);
    }

    @Test
    void testSearchRestaurantsWithKeywordSuccess() {
        // Given
        String keyword = "Pho";
        Pageable pageable = PageRequest.of(0, 10);
        List<Restaurant> restaurants = Arrays.asList(restaurant);
        Page<Restaurant> restaurantPage = new PageImpl<>(restaurants, pageable, 1);
        
        when(restaurantRepository.searchByKeyword(keyword, pageable)).thenReturn(restaurantPage);

        // When
        Page<RestaurantDTO> result = restaurantService.searchRestaurants(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).contains("Pho");
        
        verify(restaurantRepository, times(1)).searchByKeyword(keyword, pageable);
    }

    @Test
    void testSearchRestaurantsWithEmptyKeyword() {
        // Given
        String keyword = "";
        Pageable pageable = PageRequest.of(0, 10);
        List<Restaurant> restaurants = Arrays.asList(restaurant);
        Page<Restaurant> restaurantPage = new PageImpl<>(restaurants, pageable, 1);
        
        when(restaurantRepository.searchByKeyword(null, pageable)).thenReturn(restaurantPage);

        // When
        Page<RestaurantDTO> result = restaurantService.searchRestaurants(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(restaurantRepository, times(1)).searchByKeyword(null, pageable);
    }

    @Test
    void testSearchRestaurantsWithNullKeyword() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Restaurant> restaurants = Arrays.asList(restaurant);
        Page<Restaurant> restaurantPage = new PageImpl<>(restaurants, pageable, 1);
        
        when(restaurantRepository.searchByKeyword(null, pageable)).thenReturn(restaurantPage);

        // When
        Page<RestaurantDTO> result = restaurantService.searchRestaurants(null, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(restaurantRepository, times(1)).searchByKeyword(null, pageable);
    }

    @Test
    void testSearchRestaurantsNoResults() {
        // Given
        String keyword = "NonExistentRestaurant";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(restaurantRepository.searchByKeyword(keyword, pageable)).thenReturn(emptyPage);

        // When
        Page<RestaurantDTO> result = restaurantService.searchRestaurants(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        
        verify(restaurantRepository, times(1)).searchByKeyword(keyword, pageable);
    }

    @Test
    void testToDTOWithImage() {
        // Given - Restaurant with image
        restaurant.setImage("test-image.jpg");
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        RestaurantDTO result = restaurantService.createRestaurant(restaurantDTO);

        // Then - Image URL should be constructed properly
        assertThat(result.image()).isNotNull();
    }

    @Test
    void testToDTOWithNullImage() {
        // Given - Restaurant without image
        restaurant.setImage(null);
        restaurantDTO = new RestaurantDTO(1, "Pho 24", "123 Nguyen Hue", null);
        
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        RestaurantDTO result = restaurantService.createRestaurant(restaurantDTO);

        // Then - Image should be null
        assertThat(result.image()).isNull();
    }

    @Test
    void testUploadImageWithNullOldImage() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        String newImageFileName = "new-image.jpg";
        
        restaurant.setImage(null);
        
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(fileStorageService.uploadImage(mockFile, null)).thenReturn(newImageFileName);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        RestaurantDTO result = restaurantService.uploadImage(1, mockFile);

        // Then
        assertThat(result).isNotNull();
        verify(fileStorageService, times(1)).uploadImage(mockFile, null);
    }
}
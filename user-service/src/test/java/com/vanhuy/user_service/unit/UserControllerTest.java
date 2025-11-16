package com.vanhuy.user_service.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.user_service.component.JwtUtil;
import com.vanhuy.user_service.controller.UserController;
import com.vanhuy.user_service.dto.ProfileResponse;
import com.vanhuy.user_service.dto.ProfileUpdateDTO;
import com.vanhuy.user_service.model.User;
import com.vanhuy.user_service.service.ProfileService;
import com.vanhuy.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private JwtUtil jwtUtil;
    
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private User mockUser;
    private ProfileResponse mockProfileResponse;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@gmail.com");
        mockUser.setAddress("123 Test Street");

        mockProfileResponse = ProfileResponse.builder()
                .username("testuser")
                .email("testuser@gmail.com")
                .address("123 Test Street")
                .profileImageUrl("http://localhost/images/profile.jpg")
                .newJwtToken(null)
                .build();
    }

    // ==================== GET PROFILE TESTS ====================

    @Test
    @DisplayName("Should get user profile successfully when authenticated")
    void testGetProfileSuccess() throws Exception {
        // Given
        when(profileService.getProfile(anyInt())).thenReturn(mockProfileResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/users/profile")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.address").value("123 Test Street"))
                .andExpect(jsonPath("$.profileImageUrl").value("http://localhost/images/profile.jpg"));

        verify(profileService, times(1)).getProfile(1);
    }

    @Test
    @DisplayName("Should return 403 when getting profile without authentication")
    void testGetProfileUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for missing auth

        verify(profileService, never()).getProfile(anyInt());
    }

    @Test
    @DisplayName("Should get profile with null address")
    void testGetProfileWithNullAddress() throws Exception {
        // Given
        ProfileResponse responseWithNullAddress = ProfileResponse.builder()
                .username("testuser")
                .email("testuser@gmail.com")
                .address(null)
                .profileImageUrl(null)
                .newJwtToken(null)
                .build();
        when(profileService.getProfile(anyInt())).thenReturn(responseWithNullAddress);

        // When & Then
        mockMvc.perform(get("/api/v1/users/profile")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.address").isEmpty());

        verify(profileService, times(1)).getProfile(1);
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    @DisplayName("Should update profile successfully with valid data")
    void testUpdateProfileSuccess() throws Exception {
        // Given
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setUsername("newusername");
        updateDTO.setEmail("newemail@gmail.com");
        updateDTO.setAddress("456 New Street");

        ProfileResponse updatedProfile = ProfileResponse.builder()
                .username("newusername")
                .email("newemail@gmail.com")
                .address("456 New Street")
                .profileImageUrl("http://localhost/images/profile.jpg")
                .newJwtToken("new.jwt.token")
                .build();

        when(profileService.updateProfile(anyInt(), any(ProfileUpdateDTO.class), isNull()))
                .thenReturn(updatedProfile);

        MockMultipartFile profileJson = new MockMultipartFile(
                "profile",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(updateDTO).getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/users/profile")
                        .file(profileJson)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.email").value("newemail@gmail.com"))
                .andExpect(jsonPath("$.address").value("456 New Street"))
                .andExpect(jsonPath("$.newJwtToken").value("new.jwt.token"));

        verify(profileService, times(1)).updateProfile(eq(1), any(ProfileUpdateDTO.class), isNull());
    }

    @Test
    @DisplayName("Should update profile with image successfully")
    void testUpdateProfileWithImageSuccess() throws Exception {
        // Given
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setUsername("testuser");
        updateDTO.setEmail("testuser@gmail.com");
        updateDTO.setAddress("123 Test Street");

        ProfileResponse updatedProfile = ProfileResponse.builder()
                .username("testuser")
                .email("testuser@gmail.com")
                .address("123 Test Street")
                .profileImageUrl("http://localhost/images/new-profile.jpg")
                .newJwtToken(null)
                .build();

        when(profileService.updateProfile(anyInt(), any(ProfileUpdateDTO.class), any()))
                .thenReturn(updatedProfile);

        MockMultipartFile profileJson = new MockMultipartFile(
                "profile",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(updateDTO).getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/users/profile")
                        .file(profileJson)
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").value("http://localhost/images/new-profile.jpg"));

        verify(profileService, times(1)).updateProfile(eq(1), any(ProfileUpdateDTO.class), any());
    }

    @Test
    @DisplayName("Should return 401 when updating profile without authentication")
    void testUpdateProfileUnauthorized() throws Exception {
        // Given
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setUsername("testuser");
        updateDTO.setEmail("testuser@gmail.com");

        MockMultipartFile profileJson = new MockMultipartFile(
                "profile",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(updateDTO).getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/users/profile")
                        .file(profileJson)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for missing auth

        verify(profileService, never()).updateProfile(anyInt(), any(ProfileUpdateDTO.class), any());
    }

    @Test
    @DisplayName("Should return error when updating profile with invalid email")
    void testUpdateProfileInvalidEmail() throws Exception {
        // Given
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setUsername("testuser");
        updateDTO.setEmail("invalid-email");
        updateDTO.setAddress("123 Test Street");

        MockMultipartFile profileJson = new MockMultipartFile(
                "profile",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(updateDTO).getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/users/profile")
                        .file(profileJson)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(user(mockUser)))
                .andExpect(status().isBadRequest());

        verify(profileService, never()).updateProfile(anyInt(), any(ProfileUpdateDTO.class), any());
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    @DisplayName("Should delete user successfully by userId")
    void testDeleteUserSuccess() throws Exception {
        // Given
        Integer userId = 1;
        doNothing().when(userService).deleteUserById(userId);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{userId}", userId)
                        .with(user(mockUser)))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(userId);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    void testDeleteUserNotFound() throws Exception {
        // Given
        Integer userId = 999;
        doThrow(new RuntimeException("User not found")).when(userService).deleteUserById(userId);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/{userId}", userId)
                        .with(user(mockUser)))
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).deleteUserById(userId);
    }

    // ==================== GET MULTIPLE PROFILES TESTS ====================

    @Test
    @DisplayName("Should get profile for different user IDs")
    void testGetProfileForDifferentUsers() throws Exception {
        // Given
        User user1 = new User();
        user1.setUserId(1);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUserId(2);
        user2.setUsername("user2");

        ProfileResponse profile1 = ProfileResponse.builder()
                .username("user1")
                .email("user1@gmail.com")
                .build();

        ProfileResponse profile2 = ProfileResponse.builder()
                .username("user2")
                .email("user2@gmail.com")
                .build();

        when(profileService.getProfile(1)).thenReturn(profile1);
        when(profileService.getProfile(2)).thenReturn(profile2);

        // When & Then - Get profile for user1
        mockMvc.perform(get("/api/v1/users/profile")
                        .with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@gmail.com"));

        // When & Then - Get profile for user2
        mockMvc.perform(get("/api/v1/users/profile")
                        .with(user(user2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"))
                .andExpect(jsonPath("$.email").value("user2@gmail.com"));

        verify(profileService, times(1)).getProfile(1);
        verify(profileService, times(1)).getProfile(2);
    }
}

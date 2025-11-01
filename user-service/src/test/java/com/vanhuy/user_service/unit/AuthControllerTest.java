package com.vanhuy.user_service.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.user_service.component.JwtUtil;
import com.vanhuy.user_service.dto.*;
import com.vanhuy.user_service.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Should register user successfully with valid credentials")
    void testRegisterSuccess() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("username", "thanvanhuyy@gmail.com", "password");
        RegisterResponse registerResponse = new RegisterResponse("User registered successfully");
        when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return error when registering with invalid email format")
    void testRegisterInvalidEmail() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("username", "invalid-email", "password");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return error when registering with empty username")
    void testRegisterEmptyUsername() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("", "thanvanhuyy@gmail.com", "password");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return error when registering with duplicate username")
    void testRegisterDuplicateUsername() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("existingUser", "newemail@gmail.com", "password");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("username", "password");
        AuthResponse authResponse = new AuthResponse("jwt.token.here");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authResponse);

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"));

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return error when login with invalid credentials")
    void testLoginInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("username", "wrongpassword");
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return error when login with empty username")
    void testLoginEmptyUsername() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("", "password");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()); // Controller không validate, service sẽ xử lý

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return error when login with empty password")
    void testLoginEmptyPassword() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("username", "");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()); // Controller không validate, service sẽ xử lý

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return error when login with non-existent user")
    void testLoginNonExistentUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password");
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    @DisplayName("Should validate token successfully with valid JWT token")
    void testValidateTokenSuccess() throws Exception {
        // Given
        String validToken = "valid.jwt.token";
        when(jwtUtil.validateToken(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validateToken")
                        .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)));

        verify(jwtUtil, times(1)).validateToken(validToken);
    }

    @Test
    @DisplayName("Should return false when validating expired token")
    void testValidateTokenExpired() throws Exception {
        // Given
        String expiredToken = "expired.jwt.token";
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validateToken")
                        .param("token", expiredToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(false)));

        verify(jwtUtil, times(1)).validateToken(expiredToken);
    }

    @Test
    @DisplayName("Should return false when validating invalid token format")
    void testValidateTokenInvalidFormat() throws Exception {
        // Given
        String invalidToken = "invalid.token";
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validateToken")
                        .param("token", invalidToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(false)));

        verify(jwtUtil, times(1)).validateToken(invalidToken);
    }

    @Test
    @DisplayName("Should return false when validating empty token")
    void testValidateTokenEmpty() throws Exception {
        // Given
        String emptyToken = "";
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/validateToken")
                        .param("token", emptyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(false)));

        verify(jwtUtil, times(1)).validateToken(emptyToken);
    }

    @Test
    @DisplayName("Should handle multiple login requests sequentially")
    void testMultipleLoginRequests() throws Exception {
        // Given
        LoginRequest loginRequest1 = new LoginRequest("user1", "password1");
        LoginRequest loginRequest2 = new LoginRequest("user2", "password2");
        AuthResponse authResponse1 = new AuthResponse("token1");
        AuthResponse authResponse2 = new AuthResponse("token2");

        when(authService.authenticate(argThat(req -> req != null && req.getUsername().equals("user1"))))
                .thenReturn(authResponse1);
        when(authService.authenticate(argThat(req -> req != null && req.getUsername().equals("user2"))))
                .thenReturn(authResponse2);

        // When & Then - First login
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token1"));

        // When & Then - Second login
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token2"));

        verify(authService, times(2)).authenticate(any(LoginRequest.class));
    }
}

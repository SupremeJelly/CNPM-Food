package com.vanhuy.api_gateway.service;

import com.vanhuy.api_gateway.dto.ValidTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final String authServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    public AuthService(@org.springframework.beans.factory.annotation.Value("${USER_SERVICE_URL:http://localhost:8081}") String userServiceUrl) {
        this.authServiceUrl = userServiceUrl + "/api/v1/auth/validateToken";
    }

    public ValidTokenResponse validateToken(String token) {
        String url = authServiceUrl + "?token=" + token;
        logger.info("Validating token at: {}", url);

        try {
            ResponseEntity<ValidTokenResponse> response = restTemplate.postForEntity(url, null, ValidTokenResponse.class);
            logger.info("Token validation successful. Response status: {}", response.getStatusCode());
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.error("HTTP error during token validation. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return e.getStatusCode() == HttpStatus.UNAUTHORIZED ? new ValidTokenResponse(false) : null;
        } catch (RestClientException e) {
            logger.error("Error communicating with auth service", e);
            return null;
        }
    }
}
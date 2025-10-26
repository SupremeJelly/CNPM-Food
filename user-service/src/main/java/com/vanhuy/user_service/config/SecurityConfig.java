package com.vanhuy.user_service.config;

import com.vanhuy.user_service.component.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// === CÁC IMPORT CẦN THIẾT CHO CORS ===
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults; 
// ===========================================

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults()) 
            
            // === THAY THẾ KHỐI NÀY ===
            .authorizeHttpRequests(
                req -> req
                    // Chỉ định rõ ràng các phương thức POST cho auth
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/forgot").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/reset").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/validateToken").permitAll() // Thêm cái này cho an toàn

                    // Cho phép swagger
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    
                    // Tất cả các request còn lại phải xác thực
                    .anyRequest().authenticated() 
            )
            // =========================
            
            .authenticationProvider(authenticationProvider)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // === ĐỊNH NGHĨA QUY TẮC CORS ===
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép Angular (localhost:4200) gọi đến
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); 
        
        // Cho phép các phương thức này (PHẢI CÓ POST)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); 
        
        // Cho phép các header này (Content-Type là bắt buộc)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cấu hình này cho tất cả các đường dẫn
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }
}
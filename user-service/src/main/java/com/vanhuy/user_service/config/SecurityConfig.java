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

import org.springframework.http.HttpMethod;

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
            // Tắt CORS ở service vì API Gateway đã xử lý
            // .cors(withDefaults())
            
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

    // CORS đã được xử lý bởi API Gateway, không cần bean này nữa
    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     ...
    // }

}
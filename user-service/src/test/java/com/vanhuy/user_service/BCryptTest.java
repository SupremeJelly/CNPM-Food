package com.vanhuy.user_service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye6mJIAzN5kJiOdQ5Xl8JJl8UvQQ5hWoW";
        
        System.out.println("Testing password: " + rawPassword);
        System.out.println("Database hash: " + dbHash);
        System.out.println("Match result: " + encoder.matches(rawPassword, dbHash));
        
        // Generate new hash
        String newHash = encoder.encode(rawPassword);
        System.out.println("\nNew hash generated: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(rawPassword, newHash));
    }
}
